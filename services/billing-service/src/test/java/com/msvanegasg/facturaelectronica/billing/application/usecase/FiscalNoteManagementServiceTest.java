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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalDocumentUsagePort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;

@ExtendWith(MockitoExtension.class)
class FiscalNoteManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DOCUMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID NOTE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private FiscalNoteRepositoryPort noteRepository;
    @Mock
    private SaleRepositoryPort saleRepository;
    @Mock
    private FiscalNoteProviderPort providerPort;
    @Mock
    private AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    @Mock
    private IdGeneratorPort idGenerator;
    @Mock
    private ClockPort clock;

    @Test
    void createsCreditNoteForValidatedElectronicInvoice() {
        when(noteRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "note-1")).thenReturn(Optional.empty());
        when(saleRepository.findByCompanyIdAndElectronicDocumentId(COMPANY_ID, DOCUMENT_ID))
                .thenReturn(Optional.of(sale(ElectronicDocumentType.ELECTRONIC_INVOICE)));
        when(idGenerator.newId()).thenReturn(NOTE_ID);
        when(clock.now()).thenReturn(NOW);
        when(assignFiscalNumberUseCase.assign(any()))
                .thenReturn(new FiscalNumberResult(UUID.randomUUID(), "18760000002", "NC", 10));
        when(providerPort.submit(org.mockito.ArgumentMatchers.eq(COMPANY_ID), org.mockito.ArgumentMatchers.eq(NOTE_ID),
                org.mockito.ArgumentMatchers.eq(ElectronicDocumentType.CREDIT_NOTE), any(Map.class),
                org.mockito.ArgumentMatchers.eq("note-1")))
                .thenReturn(new ProviderSubmissionResult(ProviderStatus.ACCEPTED, "mock-note", "mock-cufe",
                        "mock-qr", null, null));
        when(noteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().create(new CreateFiscalNoteCommand(COMPANY_ID, DOCUMENT_ID, FiscalNoteType.CREDIT_NOTE,
                null, "Devolucion parcial", new BigDecimal("10000.00"), new BigDecimal("1900.00"),
                new BigDecimal("11900.00"), "note-1"));

        assertThat(result.id()).isEqualTo(NOTE_ID);
        assertThat(result.noteType()).isEqualTo(FiscalNoteType.CREDIT_NOTE);
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.VALIDATED);
        assertThat(result.prefix()).isEqualTo("NC");
        assertThat(result.documentNumber()).isEqualTo(10);
        assertThat(result.cufeCude()).isEqualTo("mock-cufe");
    }

    @Test
    void blocksFiscalNoteWhenMonthlyDocumentQuotaIsReached() {
        LicenseValidationPort licenseValidationPort = new LicenseValidationPort() {
            @Override
            public void ensureAllowed(UUID companyId, LicenseAction action) {
            }

            @Override
            public LicensePolicy policy(UUID companyId, LicenseAction action) {
                return new LicensePolicy(null, 1);
            }
        };
        FiscalDocumentUsagePort fiscalDocumentUsagePort = org.mockito.Mockito.mock(FiscalDocumentUsagePort.class);
        FiscalNoteManagementService quotaService = new FiscalNoteManagementService(noteRepository, saleRepository,
                providerPort, licenseValidationPort, fiscalDocumentUsagePort, assignFiscalNumberUseCase, idGenerator,
                clock);
        when(noteRepository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "note-1")).thenReturn(Optional.empty());
        when(saleRepository.findByCompanyIdAndElectronicDocumentId(COMPANY_ID, DOCUMENT_ID))
                .thenReturn(Optional.of(sale(ElectronicDocumentType.ELECTRONIC_INVOICE)));
        when(idGenerator.newId()).thenReturn(NOTE_ID);
        when(clock.now()).thenReturn(NOW);
        when(fiscalDocumentUsagePort.countIssuedDocuments(org.mockito.ArgumentMatchers.eq(COMPANY_ID), any(), any()))
                .thenReturn(1L);

        assertThatThrownBy(() -> quotaService.create(new CreateFiscalNoteCommand(COMPANY_ID, DOCUMENT_ID,
                FiscalNoteType.CREDIT_NOTE, null, "Devolucion parcial", new BigDecimal("10000.00"),
                new BigDecimal("1900.00"), new BigDecimal("11900.00"), "note-1")))
                .isInstanceOf(LicenseBlockedException.class)
                .hasMessageContaining("maximo 1 documentos");

        verify(providerPort, never()).submit(any(), any(), any(), any(), any());
        verify(noteRepository, never()).save(any());
    }

    private FiscalNoteManagementService service() {
        return new FiscalNoteManagementService(noteRepository, saleRepository, providerPort, assignFiscalNumberUseCase,
                idGenerator, clock);
    }

    private static Sale sale(ElectronicDocumentType documentType) {
        UUID productId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        Sale sale = Sale.draft(SALE_ID, COMPANY_ID, null, PaymentMethodCode.CASH, null,
                SaleChannel.ELECTRONIC_INVOICE, "sale-1", null, NOW,
                List.of(SaleLine.calculate(UUID.randomUUID(), productId, new BigDecimal("1.00"),
                        new BigDecimal("10000.00"), BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
        return sale.confirm(new ElectronicDocument(DOCUMENT_ID, COMPANY_ID, SALE_ID, documentType,
                ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED, "FE", 1, "cufe", "qr",
                new BigDecimal("10000.00"), new BigDecimal("1900.00"), new BigDecimal("11900.00"), "tracking",
                null, null, "confirm-1", NOW, NOW, NOW), NOW);
    }
}
