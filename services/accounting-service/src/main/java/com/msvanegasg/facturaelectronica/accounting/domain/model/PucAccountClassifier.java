package com.msvanegasg.facturaelectronica.accounting.domain.model;

final class PucAccountClassifier {

    private PucAccountClassifier() {
    }

    static AccountCategory categoryOf(String code) {
        return switch (code.charAt(0)) {
            case '1' -> AccountCategory.ASSET;
            case '2' -> AccountCategory.LIABILITY;
            case '3' -> AccountCategory.EQUITY;
            case '4' -> AccountCategory.INCOME;
            case '5' -> AccountCategory.EXPENSE;
            case '6' -> AccountCategory.COST_OF_SALES;
            case '7' -> AccountCategory.PRODUCTION_COST;
            case '8' -> AccountCategory.MEMORANDUM_DEBIT;
            case '9' -> AccountCategory.MEMORANDUM_CREDIT;
            default -> throw new IllegalArgumentException("account code must start with a PUC class from 1 to 9");
        };
    }

    static AccountNature natureOf(AccountCategory category) {
        return switch (category) {
            case ASSET, EXPENSE, COST_OF_SALES, PRODUCTION_COST, MEMORANDUM_DEBIT -> AccountNature.DEBIT;
            case LIABILITY, EQUITY, INCOME, MEMORANDUM_CREDIT -> AccountNature.CREDIT;
        };
    }

    static AccountLevel levelOf(String code) {
        return switch (code.length()) {
            case 1 -> AccountLevel.CLASS;
            case 2 -> AccountLevel.GROUP;
            case 4 -> AccountLevel.ACCOUNT;
            case 6 -> AccountLevel.SUBACCOUNT;
            default -> {
                if (code.length() > 6) {
                    yield AccountLevel.AUXILIARY;
                }
                throw new IllegalArgumentException(
                        "account code length must match PUC levels: 1, 2, 4, 6 or more than 6 digits");
            }
        };
    }
}
