package com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ExpenseTypeJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.PaymentMethodJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.ExpenseTypeJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.PaymentMethodJpaRepository;
import com.msvanegasg.facturaelectronica.exception.MetodoPagoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.TipoGastoNotFoundException;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseCatalogPort;
import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;

@Component
public class ExpenseCatalogAdapter implements ExpenseCatalogPort {

    private final ExpenseTypeJpaRepository expenseTypeRepository;
    private final PaymentMethodJpaRepository paymentMethodRepository;

    public ExpenseCatalogAdapter(ExpenseTypeJpaRepository expenseTypeRepository,
            PaymentMethodJpaRepository paymentMethodRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public ExpenseTypeSummary findExpenseType(Long id) {
        ExpenseTypeJpaEntity expenseType = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new TipoGastoNotFoundException(id));
        return new ExpenseTypeSummary(expenseType.getIdTipoGasto(), expenseType.getNombre(),
                expenseType.getDescripcion());
    }

    @Override
    public PaymentMethodSummary findPaymentMethod(Long id) {
        PaymentMethodJpaEntity paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));
        return new PaymentMethodSummary(paymentMethod.getIdMetodoPago(), paymentMethod.getNombre(),
                paymentMethod.getDescripcion());
    }
}
