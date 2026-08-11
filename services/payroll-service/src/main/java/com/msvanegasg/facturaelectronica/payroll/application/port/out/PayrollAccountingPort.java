package com.msvanegasg.facturaelectronica.payroll.application.port.out;

import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;

public interface PayrollAccountingPort {

    void applyDailyPayment(DailyLaborPayment payment);
}
