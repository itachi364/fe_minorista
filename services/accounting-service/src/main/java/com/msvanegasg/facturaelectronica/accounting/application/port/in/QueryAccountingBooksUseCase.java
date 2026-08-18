package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FinancialStatementResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;

public interface QueryAccountingBooksUseCase {

    JournalBookResult journalBook(JournalBookQuery query);

    LedgerBookResult ledgerBook(LedgerBookQuery query);

    FinancialStatementResult incomeStatement(LedgerBookQuery query);

    FinancialStatementResult balanceSheet(LedgerBookQuery query);
}
