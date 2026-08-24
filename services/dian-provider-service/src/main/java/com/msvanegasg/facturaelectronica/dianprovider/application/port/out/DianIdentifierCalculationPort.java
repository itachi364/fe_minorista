package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface DianIdentifierCalculationPort {

    DianIdentifierResult calculate(SubmitProviderDocumentCommand command, DianCompanyConfiguration configuration,
            String unsignedXml);
}
