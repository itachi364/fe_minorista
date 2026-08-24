package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianXmlDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface FiscalDocumentXmlBuilderPort {

    DianXmlDocument build(SubmitProviderDocumentCommand command, DianCompanyConfiguration configuration);
}
