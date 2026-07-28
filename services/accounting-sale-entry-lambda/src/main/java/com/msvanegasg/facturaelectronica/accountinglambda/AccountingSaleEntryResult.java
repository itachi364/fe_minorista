package com.msvanegasg.facturaelectronica.accountinglambda;

public record AccountingSaleEntryResult(boolean processed, boolean duplicate, boolean ignored, boolean entryCreated) {

    public static AccountingSaleEntryResult ignoredResult() {
        return new AccountingSaleEntryResult(false, false, true, false);
    }

    public static AccountingSaleEntryResult duplicateResult() {
        return new AccountingSaleEntryResult(false, true, false, false);
    }

    public static AccountingSaleEntryResult processed(boolean entryCreated) {
        return new AccountingSaleEntryResult(true, false, false, entryCreated);
    }
}