package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductPurchaseInfo;

public interface ProductLookupPort {

    ProductPurchaseInfo findByBarcode(Long barcode);
}
