package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateWithholdingsCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationResult;

public interface CalculateWithholdingsUseCase {

    WithholdingCalculationResult calculate(CalculateWithholdingsCommand command);
}
