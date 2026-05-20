package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.StockBalanceJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.StockBalanceJpaRepository;

@Component
public class StockBalancePersistenceAdapter implements StockBalanceRepositoryPort {

    private final StockBalanceJpaRepository repository;

    public StockBalancePersistenceAdapter(StockBalanceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StockBalance> findByCompanyIdAndProductId(UUID companyId, UUID productId) {
        return repository.findByCompanyIdAndProductId(companyId, productId)
                .map(StockBalancePersistenceAdapter::toDomain);
    }

    @Override
    public StockBalance save(StockBalance stockBalance) {
        return toDomain(repository.save(toEntity(stockBalance)));
    }

    private static StockBalance toDomain(StockBalanceJpaEntity entity) {
        return new StockBalance(entity.getCompanyId(), entity.getProductId(), entity.getCurrentStock(),
                entity.getReservedStock(), entity.getAverageCost(), entity.getUpdatedAt());
    }

    private static StockBalanceJpaEntity toEntity(StockBalance stockBalance) {
        StockBalanceJpaEntity entity = new StockBalanceJpaEntity();
        entity.setCompanyId(stockBalance.companyId());
        entity.setProductId(stockBalance.productId());
        entity.setCurrentStock(stockBalance.currentStock());
        entity.setReservedStock(stockBalance.reservedStock());
        entity.setAverageCost(stockBalance.averageCost());
        entity.setUpdatedAt(stockBalance.updatedAt());
        return entity;
    }
}
