package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface DianSignaturePort {

    DianSignedDocument sign(DianCompanyConfiguration configuration, String unsignedXml,
            DianIdentifierResult identifiers);
}
