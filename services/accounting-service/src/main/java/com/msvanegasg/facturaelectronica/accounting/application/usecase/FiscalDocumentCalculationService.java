package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateFiscalDocumentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateWithholdingsCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccumulationTotals;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ThirdPartyFiscalProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationItemResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateFiscalDocumentUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateWithholdingsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.CompanyTaxProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalDocumentCalculationRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

@Transactional
public class FiscalDocumentCalculationService implements CalculateFiscalDocumentUseCase {
    private final CalculateWithholdingsUseCase lineCalculator;
    private final FiscalDocumentCalculationRepositoryPort repository;
    private final CompanyTaxProfilePort companyProfilePort;
    private final ThirdPartyFiscalProfilePort thirdPartyProfilePort;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public FiscalDocumentCalculationService(CalculateWithholdingsUseCase lineCalculator,
            FiscalDocumentCalculationRepositoryPort repository, CompanyTaxProfilePort companyProfilePort,
            ThirdPartyFiscalProfilePort thirdPartyProfilePort, IdGeneratorPort idGenerator, Clock clock) {
        this.lineCalculator = Objects.requireNonNull(lineCalculator);
        this.repository = Objects.requireNonNull(repository);
        this.companyProfilePort = Objects.requireNonNull(companyProfilePort);
        this.thirdPartyProfilePort = Objects.requireNonNull(thirdPartyProfilePort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public FiscalDocumentCalculationResult calculate(CalculateFiscalDocumentCommand command) {
        validate(command);
        CompanyTaxProfile company = companyProfilePort
                .findByCompanyIdAndDate(command.companyId(), command.operationDate())
                .orElseThrow(() -> new IllegalStateException(
                        "La empresa no tiene un perfil fiscal configurado para calcular retenciones."));
        ThirdPartyFiscalProfile thirdParty = thirdPartyProfilePort
                .findByCompanyIdAndIdAndDate(command.companyId(), command.thirdPartyId(), command.operationDate())
                .filter(ThirdPartyFiscalProfile::active)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible resolver el perfil fiscal vigente del tercero."));
        String municipality = normalize(command.municipalityCode()) == null
                ? company.icaMunicipalityCode() : normalize(command.municipalityCode());
        String requestHash = hash(command, company, thirdParty, municipality);
        if (command.sourceType() != null) {
            var existing = repository.findBySource(command.companyId(), command.sourceType(), command.sourceId());
            if (existing.isPresent()) {
                if (!existing.get().requestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "El documento ya tiene un calculo fiscal confirmado con datos diferentes.");
                }
                return existing.get();
            }
            if (repository.isPeriodClosed(command.companyId(), command.operationDate())) {
                throw new IllegalStateException("El periodo fiscal de la operacion se encuentra cerrado.");
            }
            repository.lockAccumulation(command.companyId(), command.thirdPartyId(), command.operationDate());
        }

        CompanyTaxProfileCommand companyCommand = companyCommand(company);
        List<FiscalDocumentLineResult> lines = new ArrayList<>();
        Map<String, FiscalAccumulationTotals> accumulations = new LinkedHashMap<>();
        for (FiscalDocumentLineCommand line : command.lines()) {
            String conceptCode = normalizedConcept(line.conceptCode());
            FiscalAccumulationTotals accumulated = accumulations.computeIfAbsent(conceptCode,
                    ignored -> repository.findDailyAccumulation(command.companyId(), command.thirdPartyId(),
                            command.operationDate(), conceptCode));
            ThirdPartyFiscalProfileCommand thirdPartyCommand = thirdPartyCommand(thirdParty, line.ciiuCode());
            WithholdingCalculationResult result = lineCalculator.calculate(new CalculateWithholdingsCommand(
                    command.companyId(), command.operationType(), command.thirdPartyId(), conceptCode,
                    command.operationDate(), line.taxableBaseAmount(), line.taxAmount(), municipality, null, null,
                    companyCommand, thirdPartyCommand, accumulated.taxableBase(), accumulated.taxAmount(),
                    accumulated.withheldByType()));
            lines.add(new FiscalDocumentLineResult(line.lineId(), conceptCode,
                    normalize(line.ciiuCode()), money(line.taxableBaseAmount()), money(line.taxAmount()), result.items()));
            accumulations.put(conceptCode, add(accumulated, line, result.items()));
        }

        String status = status(lines);
        if (command.sourceType() != null && "BLOCKED".equals(status)) {
            String reasons = lines.stream().flatMap(line -> line.items().stream())
                    .filter(item -> item.decision() == WithholdingDecision.BLOCKED)
                    .map(WithholdingCalculationItemResult::reason).distinct()
                    .reduce((left, right) -> left + " " + right).orElse("El calculo fiscal esta bloqueado.");
            throw new IllegalStateException(reasons);
        }
        BigDecimal gross = lines.stream().map(line -> line.taxableBaseAmount().add(line.taxAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal withheld = lines.stream().flatMap(line -> line.items().stream())
                .filter(item -> item.decision() == WithholdingDecision.APPLIED)
                .filter(item -> item.withholdingType() != WithholdingType.AUTORETENCION)
                .map(WithholdingCalculationItemResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        FiscalDocumentCalculationResult result = new FiscalDocumentCalculationResult(idGenerator.newId(),
                command.companyId(), command.operationType(), command.thirdPartyId(), command.operationDate(),
                municipality, command.sourceType(), command.sourceId(), requestHash, status, lines, gross, withheld,
                gross.subtract(withheld).setScale(2, RoundingMode.HALF_UP), evidence(company, thirdParty),
                Instant.now(clock));
        if (command.sourceType() == null) {
            return result;
        }
        FiscalDocumentCalculationResult saved = repository.save(result);
        repository.saveAccumulations(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FiscalDocumentCalculationResult> findBySource(UUID companyId,
            com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType sourceType,
            UUID sourceId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(sourceType, "sourceType is required");
        Objects.requireNonNull(sourceId, "sourceId is required");
        return repository.findBySource(companyId, sourceType, sourceId);
    }

    private static void validate(CalculateFiscalDocumentCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.operationType(), "operationType is required");
        Objects.requireNonNull(command.thirdPartyId(), "thirdPartyId is required");
        Objects.requireNonNull(command.operationDate(), "operationDate is required");
        if ((command.sourceType() == null) != (command.sourceId() == null)) {
            throw new IllegalArgumentException("sourceType and sourceId must be provided together");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
        java.util.HashSet<UUID> ids = new java.util.HashSet<>();
        command.lines().forEach(line -> {
            Objects.requireNonNull(line.lineId(), "lineId is required");
            if (!ids.add(line.lineId())) throw new IllegalArgumentException("lineId cannot be duplicated");
            Objects.requireNonNull(line.taxableBaseAmount(), "taxableBaseAmount is required");
            Objects.requireNonNull(line.taxAmount(), "taxAmount is required");
            if (line.taxableBaseAmount().signum() < 0 || line.taxAmount().signum() < 0) {
                throw new IllegalArgumentException("line amounts cannot be negative");
            }
        });
    }

    private static String status(List<FiscalDocumentLineResult> lines) {
        if (lines.stream().flatMap(line -> line.items().stream())
                .anyMatch(item -> item.decision() == WithholdingDecision.BLOCKED)) return "BLOCKED";
        if (lines.stream().flatMap(line -> line.items().stream())
                .anyMatch(item -> item.decision() == WithholdingDecision.APPLIED)) return "APPLIED";
        return "NOT_APPLIED";
    }

    private static CompanyTaxProfileCommand companyCommand(CompanyTaxProfile profile) {
        return new CompanyTaxProfileCommand(profile.taxRegime(), profile.rutResponsibilities(),
                profile.vatResponsible(), profile.withholdingAgent(), profile.largeTaxpayer(), profile.selfWithholding(),
                profile.simpleRegime(), profile.icaMunicipalityCode(), profile.ciiuCodes(),
                profile.vatWithholdingAgent(), profile.icaWithholdingAgent());
    }

    private static ThirdPartyFiscalProfileCommand thirdPartyCommand(ThirdPartyFiscalProfile profile,
            String lineCiiu) {
        LinkedHashSet<String> ciiuCodes = new LinkedHashSet<>(profile.ciiuCodes());
        if (normalize(lineCiiu) != null) ciiuCodes.add(normalize(lineCiiu));
        return new ThirdPartyFiscalProfileCommand(profile.thirdPartyId(), profile.taxRegime(),
                profile.taxResponsibilities(), profile.municipalityCode(), profile.ciiuCode(), ciiuCodes,
                profile.active());
    }

    private static Map<String, Object> evidence(CompanyTaxProfile company, ThirdPartyFiscalProfile thirdParty) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("companyTaxRegime", Objects.toString(company.taxRegime(), ""));
        values.put("companyResponsibilities", company.rutResponsibilities());
        values.put("companyWithholdingAgent", company.withholdingAgent());
        values.put("companyVatWithholdingAgent", company.vatWithholdingAgent());
        values.put("companyIcaWithholdingAgent", company.icaWithholdingAgent());
        values.put("companySelfWithholding", company.selfWithholding());
        values.put("thirdPartyTaxRegime", Objects.toString(thirdParty.taxRegime(), ""));
        values.put("thirdPartyResponsibilities", thirdParty.taxResponsibilities());
        values.put("thirdPartyMunicipalityCode", Objects.toString(thirdParty.municipalityCode(), ""));
        values.put("thirdPartyCiiuCodes", thirdParty.ciiuCodes());
        return Map.copyOf(values);
    }

    private static String hash(CalculateFiscalDocumentCommand command, CompanyTaxProfile company,
            ThirdPartyFiscalProfile thirdParty, String municipality) {
        String canonical = command.companyId() + "|" + command.operationType() + "|" + command.thirdPartyId() + "|"
                + command.operationDate() + "|" + municipality + "|" + command.lines() + "|" + company + "|"
                + thirdParty;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no esta disponible", exception);
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static FiscalAccumulationTotals add(FiscalAccumulationTotals current, FiscalDocumentLineCommand line,
            List<WithholdingCalculationItemResult> items) {
        Map<WithholdingType, BigDecimal> withheld = new EnumMap<>(WithholdingType.class);
        withheld.putAll(current.withheldByType());
        items.stream().filter(item -> item.decision() == WithholdingDecision.APPLIED)
                .forEach(item -> withheld.merge(item.withholdingType(), item.amount(), BigDecimal::add));
        return new FiscalAccumulationTotals(current.taxableBase().add(money(line.taxableBaseAmount())),
                current.taxAmount().add(money(line.taxAmount())), withheld);
    }

    private static String normalizedConcept(String value) {
        String normalized = normalize(value);
        return normalized == null ? "ANY" : normalized;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
