package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StockBalanceId implements Serializable {

    private UUID companyId;
    private UUID productId;

    public StockBalanceId() {
    }

    public StockBalanceId(UUID companyId, UUID productId) {
        this.companyId = companyId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof StockBalanceId that)) {
            return false;
        }
        return Objects.equals(companyId, that.companyId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, productId);
    }
}
