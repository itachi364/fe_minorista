package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianValidationReport;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface DianTechnicalValidationPort {

    DianValidationReport validate(UUID submissionId, SubmitProviderDocumentCommand command,
            DianCompanyConfiguration configuration, String unsignedXml, DianSignedDocument signedDocument,
            DianIdentifierResult identifiers);
}
