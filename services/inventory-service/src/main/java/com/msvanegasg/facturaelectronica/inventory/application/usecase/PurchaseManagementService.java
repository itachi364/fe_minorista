package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreatePurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

import java.util.UUID;

public class PurchaseManagementService implements ManagePurchaseUseCase {

    private final PurchaseRepositoryPort purchaseRepository;
    private final PurchaseAccountingPort purchaseAccountingPort;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public PurchaseManagementService(PurchaseRepositoryPort purchaseRepository,
            PurchaseAccountingPort purchaseAccountingPort, IdGeneratorPort idGenerator, ClockPort clock) {
        this.purchaseRepository = Objects.requireNonNull(purchaseRepository);
        this.purchaseAccountingPort = Objects.requireNonNull(purchaseAccountingPort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PurchaseResult create(CreatePurchaseCommand command) {
        validate(command);
        return purchaseRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey())
                .map(InventoryResultMapper::toPurchaseResult)
                .orElseGet(() -> createNew(command));
    }

    @Override
    public List<PurchaseResult> find(PurchaseQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("from cannot be after to");
        }
        return purchaseRepository.find(query).stream().map(InventoryResultMapper::toPurchaseResult).toList();
    }

    @Override
    public PurchaseResult confirm(UUID companyId, UUID purchaseId, UUID createdBy) {
        Purchase purchase = purchaseRepository.findByCompanyIdAndId(companyId, purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
        if (purchase.status() == PurchaseStatus.CONFIRMED) {
            return InventoryResultMapper.toPurchaseResult(purchase);
        }
        Purchase confirmed = purchaseRepository.save(purchase.confirm(clock.now()));
        purchaseAccountingPort.applyConfirmedPurchase(confirmed, createdBy);
        return InventoryResultMapper.toPurchaseResult(confirmed);
    }

    private PurchaseResult createNew(CreatePurchaseCommand command) {
        UUID purchaseId = idGenerator.newId();
        var lines = command.lines().stream().map(line -> toLine(purchaseId, line)).toList();
        PaymentCondition paymentCondition = command.paymentCondition() == null ? PaymentCondition.CASH
                : command.paymentCondition();
        Purchase purchase = Purchase.pending(purchaseId, command.companyId(), command.supplierId(), command.subtotal(),
                command.taxTotal(), command.total(), paymentCondition, command.dueDate(), command.evidenceUrl(),
                command.idempotencyKey(), clock.now(), lines);
        return InventoryResultMapper.toPurchaseResult(purchaseRepository.save(purchase));
    }

    private PurchaseLine toLine(UUID purchaseId, PurchaseLineCommand command) {
        return new PurchaseLine(idGenerator.newId(), purchaseId, command.productId(), command.description(), command.quantity(),
                command.unitCost(), command.subtotal(), command.tax(), command.total());
    }

    private static void validate(CreatePurchaseCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");
        Objects.requireNonNull(command.lines(), "lines are required");
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
    }
}
