package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.MunicipalReteicaRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalFiscalPackageRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalRuleSetStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

class MunicipalFiscalPackageServiceTest {
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void createsDraftFromDivipolaWithoutPrecreatingOtherMunicipalities() {
        InMemoryRepository repository = new InMemoryRepository();
        MunicipalFiscalPackageService service = service(repository);

        MunicipalFiscalRulePackage created = service.createDraft(command("11001"));

        assertThat(created.status()).isEqualTo(FiscalRuleSetStatus.DRAFT);
        assertThat(created.municipalityCode()).isEqualTo("11001");
        assertThat(repository.packages).hasSize(1);
        assertThat(repository.packages).noneMatch(item -> !"11001".equals(item.municipalityCode()));
    }

    @Test
    void validatesAndImportsCommaDelimitedCsvAtomically() {
        InMemoryRepository repository = new InMemoryRepository();
        MunicipalFiscalPackageService service = service(repository);
        String csv = MunicipalFiscalPackageService.CSV_HEADER + "\n"
                + "11001,BOG-RETEICA,2026-1,PURCHASE,SERVICE,6201,0.00966,COP,0,GTE,TAXABLE_BASE,2026-01-01,,"
                + "\"Acuerdo 1, articulo 2\",https://bogota.gov.co/acuerdo\n";

        assertThat(service.validateCsv("reteica.csv", csv.getBytes(StandardCharsets.UTF_8)).valid()).isTrue();
        List<MunicipalFiscalRulePackage> imported = service.importCsv("reteica.csv",
                csv.getBytes(StandardCharsets.UTF_8), USER_ID);

        assertThat(imported).singleElement().satisfies(item -> {
            assertThat(item.importId()).isNotNull();
            assertThat(item.ruleCount()).isEqualTo(1);
        });
        assertThat(repository.imports).hasSize(1);
    }

    @Test
    void rejectsEntireInvalidCsvAndDoesNotPersistAnything() {
        InMemoryRepository repository = new InMemoryRepository();
        MunicipalFiscalPackageService service = service(repository);
        String csv = MunicipalFiscalPackageService.CSV_HEADER + "\n"
                + "99999,TEST,1,PURCHASE,ANY,,0.01,COP,0,GTE,TAXABLE_BASE,2026-01-01,,Norma,http://invalid.test\n";

        assertThat(service.validateCsv("reteica.csv", csv.getBytes(StandardCharsets.UTF_8)).valid()).isFalse();
        assertThatThrownBy(() -> service.importCsv("reteica.csv", csv.getBytes(StandardCharsets.UTF_8), USER_ID))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repository.packages).isEmpty();
        assertThat(repository.imports).isEmpty();
    }

    @Test
    void refusesPublicationWhenAnotherPublishedPeriodOverlaps() {
        InMemoryRepository repository = new InMemoryRepository();
        MunicipalFiscalPackageService service = service(repository);
        MunicipalFiscalRulePackage draft = service.createDraft(command("11001"));
        repository.overlap = true;

        assertThatThrownBy(() -> service.publish(draft.id(), USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("solapa");
        assertThat(repository.packages.get(0).status()).isEqualTo(FiscalRuleSetStatus.DRAFT);
    }

    private static MunicipalFiscalPackageService service(InMemoryRepository repository) {
        AtomicInteger sequence = new AtomicInteger();
        return new MunicipalFiscalPackageService(repository,
                code -> "11001".equals(code)
                        ? Optional.of(new MunicipalityReference("11001", "11", "Bogota, D.C.", true))
                        : Optional.empty(),
                () -> UUID.nameUUIDFromBytes(("reteica-" + sequence.incrementAndGet()).getBytes(StandardCharsets.UTF_8)));
    }

    private static CreateMunicipalFiscalPackageCommand command(String municipality) {
        return new CreateMunicipalFiscalPackageCommand(municipality, "BOG-RETEICA", "2026-1",
                LocalDate.of(2026, 1, 1), null, "Acuerdo 1", "https://bogota.gov.co/acuerdo",
                List.of(new MunicipalReteicaRuleCommand(FiscalOperationType.PURCHASE, "SERVICE", "6201",
                        new BigDecimal("0.00966"), FiscalThresholdUnit.COP, BigDecimal.ZERO,
                        FiscalThresholdOperator.GTE, FiscalCalculationBase.TAXABLE_BASE)), USER_ID);
    }

    private static final class InMemoryRepository implements MunicipalFiscalPackageRepositoryPort {
        private final List<MunicipalFiscalRulePackage> packages = new ArrayList<>();
        private final List<UUID> imports = new ArrayList<>();
        private boolean overlap;

        @Override public List<MunicipalFiscalRulePackage> findAll() { return List.copyOf(packages); }
        @Override public Optional<MunicipalFiscalRulePackage> findById(UUID id) {
            return packages.stream().filter(item -> item.id().equals(id)).findFirst();
        }
        @Override public FiscalLegalSource findOrCreateSource(CreateMunicipalFiscalPackageCommand command,
                UUID sourceId) {
            return new FiscalLegalSource(sourceId, "SRC", command.legalReference(), "Municipio",
                    command.officialSourceUrl(), command.validFrom(), command.validTo(), true, Instant.now(), USER_ID);
        }
        @Override public MunicipalFiscalRulePackage createDraft(UUID id, CreateMunicipalFiscalPackageCommand command,
                MunicipalityReference municipality, UUID legalSourceId, UUID importId, List<UUID> ruleIds) {
            MunicipalFiscalRulePackage result = new MunicipalFiscalRulePackage(id, command.packageCode(),
                    command.version(), municipality.code(), municipality.name(), legalSourceId,
                    FiscalRuleSetStatus.DRAFT, command.validFrom(), command.validTo(), null, null, null, importId,
                    Instant.now(), command.userId(), ruleIds.size());
            packages.add(result);
            return result;
        }
        @Override public MunicipalFiscalRulePackage publish(UUID id, UUID userId) {
            MunicipalFiscalRulePackage current = findById(id).orElseThrow();
            MunicipalFiscalRulePackage published = new MunicipalFiscalRulePackage(current.id(), current.code(),
                    current.version(), current.municipalityCode(), current.municipalityName(), current.legalSourceId(),
                    FiscalRuleSetStatus.PUBLISHED, current.validFrom(), current.validTo(), Instant.now(), Instant.now(),
                    userId, current.importId(), current.createdAt(), current.createdBy(), current.ruleCount());
            packages.set(packages.indexOf(current), published);
            return published;
        }
        @Override public boolean hasPublishedOverlap(MunicipalFiscalRulePackage candidate) { return overlap; }
        @Override public UUID saveImport(String fileName, String sha256, int rows, int packageCount, String status,
                String errorDetail, UUID userId) {
            UUID id = UUID.randomUUID(); imports.add(id); return id;
        }
    }
}
