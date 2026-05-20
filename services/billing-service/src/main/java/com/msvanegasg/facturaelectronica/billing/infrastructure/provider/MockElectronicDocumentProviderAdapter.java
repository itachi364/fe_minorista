package com.msvanegasg.facturaelectronica.billing.infrastructure.provider;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CudeGenerator;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

public class MockElectronicDocumentProviderAdapter implements ElectronicDocumentProviderPort {

    private final BillingProperties properties;

    public MockElectronicDocumentProviderAdapter(BillingProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProviderSubmissionResult submitElectronicPos(Sale sale, UUID documentId, String idempotencyKey) {
        ProviderStatus status = ProviderStatus.valueOf(properties.mockProviderDefaultStatus());
        String cude = CudeGenerator.generate(sale.companyId() + "|" + documentId + "|" + idempotencyKey);
        if (status == ProviderStatus.ACCEPTED) {
            return new ProviderSubmissionResult(status, "mock-" + documentId, cude, "mock-qr:" + cude, null, null);
        }
        if (status == ProviderStatus.REJECTED) {
            return new ProviderSubmissionResult(status, "mock-" + documentId, null, null, "MOCK_REJECTED",
                    "Documento rechazado por proveedor mock.");
        }
        return new ProviderSubmissionResult(status, "mock-" + documentId, null, null, "MOCK_FAILED",
                "Fallo tecnico simulado por proveedor mock.");
    }
}
