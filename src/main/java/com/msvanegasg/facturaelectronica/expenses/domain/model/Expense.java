package com.msvanegasg.facturaelectronica.expenses.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.enums.Estado;

public final class Expense {

    private final Long id;
    private final LocalDateTime date;
    private final BigDecimal amount;
    private final String description;
    private final ExpenseTypeSummary expenseType;
    private final PaymentMethodSummary paymentMethod;
    private final String evidenceUrl;
    private final Estado status;
    private final boolean active;

    private Expense(Long id, LocalDateTime date, BigDecimal amount, String description,
            ExpenseTypeSummary expenseType, PaymentMethodSummary paymentMethod, String evidenceUrl, Estado status,
            boolean active) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.expenseType = expenseType;
        this.paymentMethod = paymentMethod;
        this.evidenceUrl = evidenceUrl;
        this.status = status;
        this.active = active;
    }

    public static Expense create(LocalDateTime date, BigDecimal amount, String description,
            ExpenseTypeSummary expenseType, PaymentMethodSummary paymentMethod, String evidenceUrl, Estado status) {
        return new Expense(null, requireDate(date), requireNonNegative(amount), normalizeOptional(description),
                requireExpenseType(expenseType), requirePaymentMethod(paymentMethod), normalizeOptional(evidenceUrl),
                Objects.requireNonNull(status, "status is required"), true);
    }

    public static Expense restore(Long id, LocalDateTime date, BigDecimal amount, String description,
            ExpenseTypeSummary expenseType, PaymentMethodSummary paymentMethod, String evidenceUrl, Estado status,
            boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Expense(id, requireDate(date), requireNonNegative(amount), normalizeOptional(description),
                requireExpenseType(expenseType), requirePaymentMethod(paymentMethod), normalizeOptional(evidenceUrl),
                Objects.requireNonNull(status, "status is required"), active);
    }

    public Expense update(LocalDateTime date, BigDecimal amount, String description, ExpenseTypeSummary expenseType,
            PaymentMethodSummary paymentMethod, String evidenceUrl, Estado status) {
        return new Expense(id, requireDate(date), requireNonNegative(amount), normalizeOptional(description),
                requireExpenseType(expenseType), requirePaymentMethod(paymentMethod), normalizeOptional(evidenceUrl),
                Objects.requireNonNull(status, "status is required"), active);
    }

    public Expense disable() {
        return new Expense(id, date, amount, description, expenseType, paymentMethod, evidenceUrl, status, false);
    }

    public Long id() {
        return id;
    }

    public LocalDateTime date() {
        return date;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String description() {
        return description;
    }

    public ExpenseTypeSummary expenseType() {
        return expenseType;
    }

    public PaymentMethodSummary paymentMethod() {
        return paymentMethod;
    }

    public String evidenceUrl() {
        return evidenceUrl;
    }

    public Estado status() {
        return status;
    }

    public boolean active() {
        return active;
    }

    private static LocalDateTime requireDate(LocalDateTime value) {
        return Objects.requireNonNull(value, "date is required");
    }

    private static BigDecimal requireNonNegative(BigDecimal value) {
        Objects.requireNonNull(value, "amount is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("amount must be zero or positive");
        }
        return value;
    }

    private static ExpenseTypeSummary requireExpenseType(ExpenseTypeSummary value) {
        return Objects.requireNonNull(value, "expenseType is required");
    }

    private static PaymentMethodSummary requirePaymentMethod(PaymentMethodSummary value) {
        return Objects.requireNonNull(value, "paymentMethod is required");
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
