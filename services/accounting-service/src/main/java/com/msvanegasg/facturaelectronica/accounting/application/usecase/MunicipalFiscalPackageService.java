package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalCsvValidationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.MunicipalReteicaRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageMunicipalFiscalPackagesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalFiscalPackageRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalityCatalogPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalRuleSetStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

@Transactional
public class MunicipalFiscalPackageService implements ManageMunicipalFiscalPackagesUseCase {
    static final String CSV_HEADER = "municipalityDivipolaCode,packageCode,version,operationType,conceptCode,ciiuCode,rate,"
            + "thresholdUnit,thresholdValue,thresholdOperator,calculationBase,validFrom,validTo,legalReference,"
            + "officialSourceUrl";
    private static final int MAX_CSV_BYTES = 5 * 1024 * 1024;

    private final MunicipalFiscalPackageRepositoryPort repository;
    private final MunicipalityCatalogPort municipalityCatalog;
    private final IdGeneratorPort idGenerator;

    public MunicipalFiscalPackageService(MunicipalFiscalPackageRepositoryPort repository,
            MunicipalityCatalogPort municipalityCatalog, IdGeneratorPort idGenerator) {
        this.repository = Objects.requireNonNull(repository);
        this.municipalityCatalog = Objects.requireNonNull(municipalityCatalog);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MunicipalFiscalRulePackage> findAll() {
        return repository.findAll();
    }

    @Override
    public MunicipalFiscalRulePackage createDraft(CreateMunicipalFiscalPackageCommand command) {
        return createDraft(command, null);
    }

    @Override
    @Transactional(readOnly = true)
    public FiscalCsvValidationResult validateCsv(String fileName, byte[] content) {
        CsvParseResult result = parseAndValidate(fileName, content);
        return new FiscalCsvValidationResult(result.errors().isEmpty(), result.rowCount(), result.commands().size(),
                result.errors());
    }

    @Override
    public List<MunicipalFiscalRulePackage> importCsv(String fileName, byte[] content, UUID userId) {
        CsvParseResult result = parseAndValidate(fileName, content);
        if (!result.errors().isEmpty()) {
            throw new IllegalArgumentException("CSV invalido: " + String.join(" | ", result.errors()));
        }
        UUID importId = repository.saveImport(normalizeFileName(fileName), sha256(content), result.rowCount(),
                result.commands().size(), "IMPORTED", null, userId);
        return result.commands().stream()
                .map(command -> createDraft(withUser(command, userId), importId))
                .toList();
    }

    @Override
    public MunicipalFiscalRulePackage publish(UUID packageId, UUID userId) {
        MunicipalFiscalRulePackage current = repository.findById(packageId)
                .orElseThrow(() -> new IllegalStateException("El paquete fiscal municipal no existe."));
        if (current.status() != FiscalRuleSetStatus.DRAFT) {
            throw new IllegalStateException("Solo se puede publicar un paquete en borrador.");
        }
        if (repository.hasPublishedOverlap(current)) {
            throw new IllegalStateException("Ya existe un paquete ReteICA publicado que se solapa para el municipio.");
        }
        return repository.publish(packageId, userId);
    }

    private MunicipalFiscalRulePackage createDraft(CreateMunicipalFiscalPackageCommand command, UUID importId) {
        validateCommand(command);
        MunicipalityReference municipality = municipalityCatalog.findActive(command.municipalityCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("El municipio DIVIPOLA no existe o esta inactivo."));
        UUID sourceId = idGenerator.newId();
        UUID legalSourceId = repository.findOrCreateSource(command, sourceId).id();
        List<UUID> ruleIds = command.rules().stream().map(ignored -> idGenerator.newId()).toList();
        return repository.createDraft(idGenerator.newId(), command, municipality, legalSourceId, importId, ruleIds);
    }

    private CsvParseResult parseAndValidate(String fileName, byte[] content) {
        List<String> errors = new ArrayList<>();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            errors.add("El archivo debe tener extension .csv.");
        }
        if (content == null || content.length == 0) {
            errors.add("El archivo CSV esta vacio.");
            return new CsvParseResult(0, List.of(), errors);
        }
        if (content.length > MAX_CSV_BYTES) {
            errors.add("El archivo CSV supera el limite de 5 MB.");
            return new CsvParseResult(0, List.of(), errors);
        }
        List<List<String>> records;
        try {
            records = CsvParser.parse(new String(content, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            errors.add(exception.getMessage());
            return new CsvParseResult(0, List.of(), errors);
        }
        if (records.isEmpty() || !CSV_HEADER.equals(String.join(",", records.get(0)))) {
            errors.add("El encabezado no coincide con la plantilla oficial.");
            return new CsvParseResult(Math.max(0, records.size() - 1), List.of(), errors);
        }
        Map<PackageKey, PackageBuilder> packages = new LinkedHashMap<>();
        Set<String> validatedMunicipalities = new LinkedHashSet<>();
        for (int index = 1; index < records.size(); index++) {
            List<String> row = records.get(index);
            if (row.stream().allMatch(String::isBlank)) {
                continue;
            }
            int line = index + 1;
            if (row.size() != 15) {
                errors.add("Linea " + line + ": se esperaban 15 columnas.");
                continue;
            }
            try {
                String municipalityCode = required(row.get(0), "municipalityDivipolaCode");
                if (validatedMunicipalities.add(municipalityCode)
                        && municipalityCatalog.findActive(municipalityCode).isEmpty()) {
                    errors.add("Linea " + line + ": municipio DIVIPOLA inexistente o inactivo.");
                }
                PackageKey key = new PackageKey(municipalityCode, required(row.get(1), "packageCode"),
                        required(row.get(2), "version"), LocalDate.parse(required(row.get(11), "validFrom")),
                        optionalDate(row.get(12)), required(row.get(13), "legalReference"),
                        requiredHttps(row.get(14)));
                MunicipalReteicaRuleCommand rule = new MunicipalReteicaRuleCommand(FiscalOperationType.valueOf(
                        required(row.get(3), "operationType").toUpperCase(Locale.ROOT)),
                        defaultAny(row.get(4)), normalize(row.get(5)), decimal(row.get(6), "rate"),
                        FiscalThresholdUnit.valueOf(required(row.get(7), "thresholdUnit").toUpperCase(Locale.ROOT)),
                        decimal(row.get(8), "thresholdValue"), FiscalThresholdOperator.valueOf(
                                required(row.get(9), "thresholdOperator").toUpperCase(Locale.ROOT)),
                        FiscalCalculationBase.valueOf(required(row.get(10), "calculationBase").toUpperCase(Locale.ROOT)));
                packages.computeIfAbsent(key, PackageBuilder::new).rules.add(rule);
            } catch (RuntimeException exception) {
                errors.add("Linea " + line + ": " + exception.getMessage());
            }
        }
        List<CreateMunicipalFiscalPackageCommand> commands = packages.values().stream()
                .map(PackageBuilder::command)
                .toList();
        commands.forEach(command -> {
            try {
                validateCommand(command);
            } catch (RuntimeException exception) {
                errors.add(command.packageCode() + ": " + exception.getMessage());
            }
        });
        return new CsvParseResult(Math.max(0, records.size() - 1), commands, List.copyOf(errors));
    }

    private static void validateCommand(CreateMunicipalFiscalPackageCommand command) {
        Objects.requireNonNull(command, "command is required");
        required(command.municipalityCode(), "municipalityCode");
        String code = required(command.packageCode(), "packageCode");
        String version = required(command.version(), "version");
        if ((code + "-" + version).length() > 40) {
            throw new IllegalArgumentException("packageCode y version superan 40 caracteres combinados");
        }
        Objects.requireNonNull(command.validFrom(), "validFrom is required");
        if (command.validTo() != null && command.validTo().isBefore(command.validFrom())) {
            throw new IllegalArgumentException("validTo no puede ser anterior a validFrom");
        }
        required(command.legalReference(), "legalReference");
        requiredHttps(command.officialSourceUrl());
        if (command.rules() == null || command.rules().isEmpty()) {
            throw new IllegalArgumentException("El paquete debe incluir al menos una regla.");
        }
        command.rules().forEach(rule -> {
            Objects.requireNonNull(rule.operationType(), "operationType is required");
            Objects.requireNonNull(rule.rate(), "rate is required");
            Objects.requireNonNull(rule.thresholdValue(), "thresholdValue is required");
            if (rule.rate().signum() < 0 || rule.rate().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("La tarifa debe ser decimal entre 0 y 1.");
            }
            if (rule.thresholdValue().signum() < 0) {
                throw new IllegalArgumentException("El umbral no puede ser negativo.");
            }
        });
    }

    private static CreateMunicipalFiscalPackageCommand withUser(CreateMunicipalFiscalPackageCommand command,
            UUID userId) {
        return new CreateMunicipalFiscalPackageCommand(command.municipalityCode(), command.packageCode(),
                command.version(), command.validFrom(), command.validTo(), command.legalReference(),
                command.officialSourceUrl(), command.rules(), userId);
    }

    private static BigDecimal decimal(String value, String name) {
        try {
            return new BigDecimal(required(value, name));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " no es un numero valido");
        }
    }

    private static LocalDate optionalDate(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : LocalDate.parse(normalized);
    }

    private static String requiredHttps(String value) {
        String required = required(value, "officialSourceUrl");
        if (!required.toLowerCase(Locale.ROOT).startsWith("https://")) {
            throw new IllegalArgumentException("officialSourceUrl debe usar HTTPS");
        }
        return required;
    }

    private static String required(String value, String name) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
        return normalized;
    }

    private static String defaultAny(String value) {
        String normalized = normalize(value);
        return normalized == null ? "ANY" : normalized;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeFileName(String fileName) {
        String normalized = fileName == null ? "reteica.csv" : fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        normalized = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return normalized.length() <= 250 ? normalized : normalized.substring(normalized.length() - 250);
    }

    private static String sha256(byte[] content) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no esta disponible", exception);
        }
    }

    private record CsvParseResult(int rowCount, List<CreateMunicipalFiscalPackageCommand> commands,
            List<String> errors) { }

    private record PackageKey(String municipalityCode, String packageCode, String version, LocalDate validFrom,
            LocalDate validTo, String legalReference, String officialSourceUrl) { }

    private static final class PackageBuilder {
        private final PackageKey key;
        private final List<MunicipalReteicaRuleCommand> rules = new ArrayList<>();
        private PackageBuilder(PackageKey key) { this.key = key; }
        private CreateMunicipalFiscalPackageCommand command() {
            return new CreateMunicipalFiscalPackageCommand(key.municipalityCode(), key.packageCode(), key.version(),
                    key.validFrom(), key.validTo(), key.legalReference(), key.officialSourceUrl(), List.copyOf(rules),
                    null);
        }
    }

    static final class CsvParser {
        private CsvParser() { }

        static List<List<String>> parse(String content) {
            String value = content != null && content.startsWith("\uFEFF") ? content.substring(1) : content;
            List<List<String>> records = new ArrayList<>();
            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            for (int index = 0; index < value.length(); index++) {
                char current = value.charAt(index);
                if (current == '"') {
                    if (quoted && index + 1 < value.length() && value.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = !quoted;
                    }
                } else if (current == ',' && !quoted) {
                    row.add(field.toString().trim());
                    field.setLength(0);
                } else if ((current == '\n' || current == '\r') && !quoted) {
                    if (current == '\r' && index + 1 < value.length() && value.charAt(index + 1) == '\n') index++;
                    row.add(field.toString().trim());
                    field.setLength(0);
                    records.add(List.copyOf(row));
                    row.clear();
                } else {
                    field.append(current);
                }
            }
            if (quoted) throw new IllegalArgumentException("El CSV contiene una comilla sin cerrar.");
            if (!field.isEmpty() || !row.isEmpty()) {
                row.add(field.toString().trim());
                records.add(List.copyOf(row));
            }
            return records;
        }
    }
}
