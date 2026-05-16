package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderRequest;
import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderResponse;

public interface DianProviderPort {

    DianProviderResponse submit(DianProviderRequest request);
}
