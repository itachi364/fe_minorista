package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AuditEventPort;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;

@ExtendWith(MockitoExtension.class)
class SaleManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID LINE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PRODUCT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
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
    private AssignFiscalNumberUseCase assignFiscalNumberUseCase;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private ClockPort clock;

    @Test
    void createsSaleWhenStockIsAvailable() {
        SaleManagementService service = service();
        when(saleRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "sale-1")).thenReturn(Optional.empty());
        when(idGenerator.newId()).thenReturn(LINE_ID, SALE_ID);
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(clock.now()).thenReturn(NOW);
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(command("sale-1"));

        assertThat(result.status()).isEqualTo(SaleStatus.DRAFT);
        assertThat(result.subtotal()).isEqualByComparingTo("30000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("5700.00");
        assertThat(result.total()).isEqualByComparingTo("35700.00");
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
        when(idGenerator.newId()).thenReturn(LINE_ID);
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(false);

        assertThatThrownBy(() -> service().create(command("sale-1")))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void confirmsSaleAndGeneratesValidatedDocument() {
        SaleManagementService service = service();
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(draftSale()));
        when(inventoryAvailability.isAvailable(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"))).thenReturn(true);
        when(providerPort.submitElectronicPos(any(), org.mockito.ArgumentMatchers.eq(DOCUMENT_ID),
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
    void confirmRetriesPendingEffectsWithoutGeneratingAnotherDocument() {
        Sale confirmed = draftSale().confirm(validatedDocument(null, null), NOW);
        when(saleRepository.findByCompanyIdAndId(COMPANY_ID, SALE_ID)).thenReturn(Optional.of(confirmed));
        when(clock.now()).thenReturn(NOW);
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().confirm(COMPANY_ID, SALE_ID, "retry-confirm");

        assertThat(result.status()).isEqualTo(SaleStatus.CONFIRMED);
        assertThat(result.electronicDocument().inventoryAppliedAt()).isEqualTo(NOW);
        assertThat(result.electronicDocument().accountingAppliedAt()).isEqualTo(NOW);
        verify(providerPort, never()).submitElectronicPos(any(), any(), any());
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
        verify(providerPort, never()).submitElectronicPos(any(), any(), any());
        verify(inventoryMovementPort, never()).applySaleOut(any(), any());
        verify(accountingEntryPort, never()).postSale(any(), any());
        verify(auditEventPort, never()).register(any());
    }

    private SaleManagementService service() {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, auditEventPort, assignFiscalNumberUseCase, idGenerator, clock);
    }

    private static CreateSaleCommand command(String idempotencyKey) {
        return new CreateSaleCommand(COMPANY_ID, null, null, SaleChannel.POS, idempotencyKey, null,
                List.of(new SaleLineCommand(PRODUCT_ID, new BigDecimal("2.00"), new BigDecimal("15000.00"),
                        BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
    }

    private static Sale draftSale() {
        return Sale.draft(SALE_ID, COMPANY_ID, null, null, SaleChannel.POS, "sale-1", null, NOW,
                List.of(com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine.calculate(LINE_ID, PRODUCT_ID,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"))));
    }

    private static ElectronicDocument validatedDocument(Instant inventoryAppliedAt, Instant accountingAppliedAt) {
        return new ElectronicDocument(DOCUMENT_ID, COMPANY_ID, SALE_ID, ElectronicDocumentType.ELECTRONIC_POS,
                ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED, "POS", 1, "mock-cude", "mock-qr",
                new BigDecimal("30000.00"), new BigDecimal("5700.00"), new BigDecimal("35700.00"), "mock-tracking",
                null, null, "confirm-1", NOW, inventoryAppliedAt, accountingAppliedAt);
    }
}
