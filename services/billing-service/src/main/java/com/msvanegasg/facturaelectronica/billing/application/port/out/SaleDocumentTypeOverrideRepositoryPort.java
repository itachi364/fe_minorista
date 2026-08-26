package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleDocumentTypeOverride;

public interface SaleDocumentTypeOverrideRepositoryPort {

    SaleDocumentTypeOverride save(SaleDocumentTypeOverride override);

    Optional<SaleDocumentTypeOverride> findActiveByCompanyIdAndSaleId(UUID companyId, UUID saleId);

    static SaleDocumentTypeOverrideRepositoryPort noop() {
        return new SaleDocumentTypeOverrideRepositoryPort() {
            @Override
            public SaleDocumentTypeOverride save(SaleDocumentTypeOverride override) {
                return override;
            }

            @Override
            public Optional<SaleDocumentTypeOverride> findActiveByCompanyIdAndSaleId(UUID companyId, UUID saleId) {
                return Optional.empty();
            }
        };
    }
}
