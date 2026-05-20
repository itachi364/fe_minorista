package com.msvanegasg.facturaelectronica.thirdparty.application.port.out;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;

public interface DocumentTypeLookupPort {

    DocumentTypeSummary findByCode(Long code);
}
