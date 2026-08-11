package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.billing.application.dto.AuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.AuditResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.InventoryProductSnapshot;
import com.msvanegasg.facturaelectronica.billing.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AuditEventPort;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalDocumentUsagePort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

@ExtendWith(MockitoExtension.class)
class SaleManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID LINE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PRODUCT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID SALE_EVENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID DOCUMENT_EVENT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID AUDIT_EVENT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Mock
    private SaleRepositoryPort saleRepository;

    @Mock
    private InventoryAvailabilityPort inventoryAvailability;

    @Mock
    private ElectronicDocumentProviderPort providerPort;

    @Mock
    private InventoryMovementPort inventoryMovementPort;

    @Mock
    private AccountingEntryPort accountingEntryPort;

    @Mock
    private AuditEventPort auditEventPort;

    @Mock
    private LicenseValidationPort licenseValidationPort;

    @Mock
    private AssignFiscalNumberUseCase assignFiscalNumberUseCase;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private ClockPort clock;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Test
    void createsSaleWhenStockIsAvailable() {
        SaleManagementService service = service();
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-1")).thenReturn(Optional.empty());
        when(inventoryAvailability.findProduct(COMPANY_ID, PRODUCT_ID)).thenReturn(productSnapshot(PRODUCT_ID));
        when(idGenerator.newId()).thenReturn(LINE_ID, SALE_ID);
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(clock.now()).thenReturn(NOW);
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(command("sale-1"));

        assertThat(result.status()).isEqualTo(SaleStatus.DRAFT);
        assertThat(result.lines().get(0).itemType()).isEqualTo(SaleItemType.PHYSICAL_GOOD);
        assertThat(result.lines().get(0).stockTracked()).isTrue();
        assertThat(result.subtotal()).isEqualByComparingTo("30000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("5700.00");
        assertThat(result.total()).isEqualByComparingTo("35700.00");
    }


    @Test
    void blocksNewSaleWhenLicenseIsNotAllowed() {
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-1")).thenReturn(Optional.empty());
        doThrow(new LicenseBlockedException("La licencia de la empresa esta suspendida."))
                .when(licenseValidationPort).ensureAllowed(COMPANY_ID, LicenseAction.CREATE_TRANSACTION);

        assertThatThrownBy(() -> service().create(command("sale-1")))
                .isInstanceOf(LicenseBlockedException.class);

        verify(inventoryAvailability, never()).findProduct(any(), any());
        verify(saleRepository, never()).save(any());
    }
    @Test
    void createIsIdempotent() {
        Sale existing = draftSale();
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-1")).thenReturn(Optional.of(existing));

        var result = service().create(command("sale-1"));

        assertThat(result.id()).isEqualTo(SALE_ID);
        verify(saleRepository, never()).save(any());
    }

    @Test
    void rejectsSaleWhenStockIsInsufficient() {
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-1")).thenReturn(Optional.empty());
        when(inventoryAvailability.findProduct(COMPANY_ID, PRODUCT_ID)).thenReturn(productSnapshot(PRODUCT_ID));
        when(idGenerator.newId()).thenReturn(LINE_ID);
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(false);

        assertThatThrownBy(() -> service().create(command("sale-1")))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void createsMixedSaleWithoutCheckingServiceStock() {
        UUID serviceProductId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID serviceLineId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "mixed-sale")).thenReturn(Optional.empty());
        when(inventoryAvailability.findProduct(COMPANY_ID, PRODUCT_ID)).thenReturn(productSnapshot(PRODUCT_ID));
        when(inventoryAvailability.findProduct(COMPANY_ID, serviceProductId)).thenReturn(serviceSnapshot(serviceProductId));
        when(idGenerator.newId()).thenReturn(LINE_ID, serviceLineId, SALE_ID);
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(clock.now()).thenReturn(NOW);
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().create(new CreateSaleCommand(COMPANY_ID, null, PaymentMethodCode.CASH, null,
                SaleChannel.POS, "mixed-sale", null, List.of(new SaleLineCommand(PRODUCT_ID, new BigDecimal("2.00"), new BigDecimal("15000.00"),
                        BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00")),
                        new SaleLineCommand(serviceProductId, BigDecimal.ONE, new BigDecimal("35000.00"),
                                BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00")))));

        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines().get(1).itemType()).isEqualTo(SaleItemType.SERVICE);
        assertThat(result.lines().get(1).stockTracked()).isFalse();
        org.mockito.Mockito.verify(inventoryAvailability, never())
                .isAvailable(COMPANY_ID, serviceProductId, BigDecimal.ONE);
    }

    @Test
    void confirmsSaleAndGeneratesValidatedDocument() {
        SaleManagementService service = service();
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(licenseValidationPort.policy(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT))
                .thenReturn(LicensePolicy.unlimited());
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(providerPort.submit(any(), org.mockito.ArgumentMatchers.eq(DOCUMENT_ID),
                org.mockito.ArgumentMatchers.eq(ElectronicDocumentType.ELECTRONIC_POS),
                org.mockito.ArgumentMatchers.eq("confirm-1")))
                .thenReturn(new ProviderSubmissionResult(ProviderStatus.ACCEPTED, "mock-tracking", "mock-cude",
                        "mock-qr", null, null));
        when(idGenerator.newId()).thenReturn(DOCUMENT_ID);
        when(clock.now()).thenReturn(NOW);
        when(assignFiscalNumberUseCase.assign(any()))
                .thenReturn(new FiscalNumberResult(UUID.randomUUID(), "18760000001", "POS", 100));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.confirm(COMPANY_ID, SALE_ID, "confirm-1");

        assertThat(result.status()).isEqualTo(SaleStatus.CONFIRMED);
        assertThat(result.electronicDocument()).isNotNull();
        assertThat(result.electronicDocument().prefix()).isEqualTo("POS");
        assertThat(result.electronicDocument().documentNumber()).isEqualTo(100);
        assertThat(result.electronicDocument().providerStatus()).isEqualTo(ProviderStatus.ACCEPTED);
        assertThat(result.electronicDocument().cufeCude()).isEqualTo("mock-cude");
        assertThat(result.electronicDocument().inventoryAppliedAt()).isEqualTo(NOW);
        assertThat(result.electronicDocument().accountingAppliedAt()).isEqualTo(NOW);
        verify(inventoryMovementPort).applySaleOut(any(), org.mockito.ArgumentMatchers.eq("confirm-1"));
        verify(accountingEntryPort).postSale(any(), org.mockito.ArgumentMatchers.eq("confirm-1"));
        ArgumentCaptor<AuditEventCommand> auditCaptor = ArgumentCaptor.forClass(AuditEventCommand.class);
        verify(auditEventPort).register(auditCaptor.capture());
        assertThat(auditCaptor.getValue().companyId()).isEqualTo(COMPANY_ID);
        assertThat(auditCaptor.getValue().eventType()).isEqualTo("ELECTRONIC_DOCUMENT");
        assertThat(auditCaptor.getValue().resourceType()).isEqualTo("SALE");
        assertThat(auditCaptor.getValue().resourceId()).isEqualTo(SALE_ID.toString());
        assertThat(auditCaptor.getValue().action()).isEqualTo("CONFIRM_SALE");
        assertThat(auditCaptor.getValue().result()).isEqualTo(AuditResult.SUCCESS);
        assertThat(auditCaptor.getValue().detail()).contains("\"documentId\":\"" + DOCUMENT_ID + "\"");
    }
    @Test
    void confirmPublishesDurableOutboxEvents() {
        SaleManagementService service = service(eventPublisher);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(licenseValidationPort.policy(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT))
                .thenReturn(LicensePolicy.unlimited());
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(providerPort.submit(any(), org.mockito.ArgumentMatchers.eq(DOCUMENT_ID),
                org.mockito.ArgumentMatchers.eq(ElectronicDocumentType.ELECTRONIC_POS),
                org.mockito.ArgumentMatchers.eq("confirm-1")))
                .thenReturn(new ProviderSubmissionResult(ProviderStatus.ACCEPTED, "mock-tracking", "mock-cude",
                        "mock-qr", null, null));
        when(idGenerator.newId()).thenReturn(DOCUMENT_ID, SALE_EVENT_ID, DOCUMENT_EVENT_ID, AUDIT_EVENT_ID);
        when(clock.now()).thenReturn(NOW);
        when(assignFiscalNumberUseCase.assign(any()))
                .thenReturn(new FiscalNumberResult(UUID.randomUUID(), "18760000001", "POS", 100));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.confirm(COMPANY_ID, SALE_ID, "confirm-1");

        ArgumentCaptor<DomainEventEnvelope> eventCaptor = ArgumentCaptor.forClass(DomainEventEnvelope.class);
        verify(eventPublisher, times(3)).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).extracting(DomainEventEnvelope::eventType)
                .containsExactly(EventTypes.SALE_CONFIRMED, EventTypes.ELECTRONIC_DOCUMENT_VALIDATED,
                        EventTypes.AUDIT_EVENT_REQUESTED);
        assertThat(eventCaptor.getAllValues().get(0).aggregateId()).isEqualTo(SALE_ID);
        assertThat(eventCaptor.getAllValues().get(0).payload()).containsEntry("documentId", DOCUMENT_ID.toString());
        assertThat(eventCaptor.getAllValues().get(1).idempotencyKey()).isEqualTo("confirm-1:document-validated");
        assertThat(eventCaptor.getAllValues().get(2).payload()).containsEntry("action", "CONFIRM_SALE");
    }

    @Test
    void confirmPublishesProviderRetryEventWhenProviderFails() {
        SaleManagementService service = service(eventPublisher);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(licenseValidationPort.policy(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT))
                .thenReturn(LicensePolicy.unlimited());
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(providerPort.submit(any(), org.mockito.ArgumentMatchers.eq(DOCUMENT_ID),
                org.mockito.ArgumentMatchers.eq(ElectronicDocumentType.ELECTRONIC_POS),
                org.mockito.ArgumentMatchers.eq("confirm-1")))
                .thenReturn(new ProviderSubmissionResult(ProviderStatus.FAILED, "mock-tracking", null, null,
                        "MOCK_FAILED", "Fallo tecnico simulado"));
        when(idGenerator.newId()).thenReturn(DOCUMENT_ID, SALE_EVENT_ID, AUDIT_EVENT_ID);
        when(clock.now()).thenReturn(NOW);
        when(assignFiscalNumberUseCase.assign(any()))
                .thenReturn(new FiscalNumberResult(UUID.randomUUID(), "18760000001", "POS", 100));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.confirm(COMPANY_ID, SALE_ID, "confirm-1");

        ArgumentCaptor<DomainEventEnvelope> eventCaptor = ArgumentCaptor.forClass(DomainEventEnvelope.class);
        verify(eventPublisher, times(2)).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).extracting(DomainEventEnvelope::eventType)
                .containsExactly(EventTypes.PROVIDER_SUBMISSION_FAILED, EventTypes.AUDIT_EVENT_REQUESTED);
        assertThat(eventCaptor.getAllValues().get(0).payload()).containsEntry("providerStatus", "FAILED");
        verify(inventoryMovementPort, never()).applySaleOut(any(), any());
        verify(accountingEntryPort, never()).postSale(any(), any());
    }

    @Test
    void blocksConfirmWhenLicenseIsNotAllowed() {
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(licenseValidationPort.policy(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT))
                .thenThrow(new LicenseBlockedException("La licencia de la empresa esta suspendida."));

        assertThatThrownBy(() -> service().confirm(COMPANY_ID, SALE_ID, "confirm-1"))
                .isInstanceOf(LicenseBlockedException.class);

        verify(providerPort, never()).submit(any(), any(), any(), any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void blocksConfirmWhenMonthlyDocumentQuotaIsReached() {
        FiscalDocumentUsagePort fiscalDocumentUsagePort = org.mockito.Mockito.mock(FiscalDocumentUsagePort.class);
        SaleManagementService service = service(fiscalDocumentUsagePort);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(licenseValidationPort.policy(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT))
                .thenReturn(new LicensePolicy(null, 1));
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(idGenerator.newId()).thenReturn(DOCUMENT_ID);
        when(clock.now()).thenReturn(NOW);
        when(fiscalDocumentUsagePort.countIssuedDocuments(org.mockito.ArgumentMatchers.eq(COMPANY_ID), any(), any()))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.confirm(COMPANY_ID, SALE_ID, "confirm-1"))
                .isInstanceOf(LicenseBlockedException.class)
                .hasMessageContaining("maximo 1 documentos");

        verify(providerPort, never()).submit(any(), any(), any(), any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void confirmRetriesPendingEffectsWithoutGeneratingAnotherDocument() {
        Sale confirmed = draftSale().confirm(validatedDocument(null, null), NOW);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(confirmed));
        when(clock.now()).thenReturn(NOW);
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().confirm(COMPANY_ID, SALE_ID, "retry-confirm");

        assertThat(result.status()).isEqualTo(SaleStatus.CONFIRMED);
        assertThat(result.electronicDocument().inventoryAppliedAt()).isEqualTo(NOW);
        assertThat(result.electronicDocument().accountingAppliedAt()).isEqualTo(NOW);
        verify(providerPort, never()).submit(any(), any(), any(), any());
        verify(inventoryMovementPort).applySaleOut(any(), org.mockito.ArgumentMatchers.eq("confirm-1"));
        verify(accountingEntryPort).postSale(any(), org.mockito.ArgumentMatchers.eq("confirm-1"));
        verify(auditEventPort, never()).register(any());
    }

    @Test
    void confirmDoesNotDuplicateAlreadyAppliedEffects() {
        Sale confirmed = draftSale().confirm(validatedDocument(NOW, NOW), NOW);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(confirmed));

        var result = service().confirm(COMPANY_ID, SALE_ID, "retry-confirm");

        assertThat(result.status()).isEqualTo(SaleStatus.CONFIRMED);
        verify(providerPort, never()).submit(any(), any(), any(), any());
        verify(inventoryMovementPort, never()).applySaleOut(any(), any());
        verify(accountingEntryPort, never()).postSale(any(), any());
        verify(auditEventPort, never()).register(any());
    }

    private SaleManagementService service() {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, auditEventPort, licenseValidationPort, assignFiscalNumberUseCase, idGenerator, clock);
    }
    private SaleManagementService service(DomainEventPublisherPort publisher) {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, auditEventPort, licenseValidationPort, assignFiscalNumberUseCase, publisher,
                idGenerator, clock);
    }

    private SaleManagementService service(FiscalDocumentUsagePort fiscalDocumentUsagePort) {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, auditEventPort,
                companyId -> Optional.of(new com.msvanegasg.facturaelectronica.billing.domain.model.FinalConsumerProfile(
                        new UUID(0L, 222L), null, "FINAL_CONSUMER", 31, "222222222222", "Consumidor final",
                        true, "TEST", "TEST", Instant.EPOCH)),
                licenseValidationPort, fiscalDocumentUsagePort, assignFiscalNumberUseCase, eventPublisher,
                idGenerator, clock);
    }

    private static CreateSaleCommand command(String idempotencyKey) {
        return new CreateSaleCommand(COMPANY_ID, null, PaymentMethodCode.CASH, null, SaleChannel.POS, idempotencyKey, null,
                List.of(new SaleLineCommand(PRODUCT_ID, new BigDecimal("2.00"), new BigDecimal("15000.00"),
                        BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
    }

    private static Sale draftSale() {
        return Sale.draft(SALE_ID, COMPANY_ID, null, PaymentMethodCode.CASH, null, SaleChannel.POS, "sale-1", null, NOW,
                List.of(com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine.calculate(LINE_ID, PRODUCT_ID,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"))));
    }

    private static InventoryProductSnapshot productSnapshot(UUID productId) {
        return new InventoryProductSnapshot(productId, "SKU-1", "Producto", SaleItemType.PHYSICAL_GOOD, true, true,
                new BigDecimal("9000.00"), new BigDecimal("10.00"));
    }

    private static InventoryProductSnapshot serviceSnapshot(UUID productId) {
        return new InventoryProductSnapshot(productId, "SERV-1", "Manicura", SaleItemType.SERVICE, true, false,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static ElectronicDocument validatedDocument(Instant inventoryAppliedAt, Instant accountingAppliedAt) {
        return new ElectronicDocument(DOCUMENT_ID, COMPANY_ID, SALE_ID, ElectronicDocumentType.ELECTRONIC_POS,
                ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED, "POS", 1, "mock-cude", "mock-qr",
                new BigDecimal("30000.00"), new BigDecimal("5700.00"), new BigDecimal("35700.00"), "mock-tracking",
                null, null, "confirm-1", NOW, inventoryAppliedAt, accountingAppliedAt);
    }
}
