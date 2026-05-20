package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

public interface PurchaseRepositoryPort {

    Purchase save(Purchase purchase);

    Optional<Purchase> findById(Long purchaseId);

    List<Purchase> findActive();

    boolean existsById(Long purchaseId);
}
