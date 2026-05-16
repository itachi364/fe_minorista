package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CheckStockAvailabilityCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;

public interface CheckStockAvailabilityUseCase {

    StockAvailabilityResult check(CheckStockAvailabilityCommand command);

    void requireAvailable(CheckStockAvailabilityCommand command);
}
