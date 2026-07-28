package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

public interface StockBalanceRepositoryPort {

    Optional<StockBalance> findByCompanyIdAndProductId(UUID companyId, UUID productId);

    List<StockBalance> findByCompanyId(UUID companyId);

    StockBalance save(StockBalance stockBalance);
}
