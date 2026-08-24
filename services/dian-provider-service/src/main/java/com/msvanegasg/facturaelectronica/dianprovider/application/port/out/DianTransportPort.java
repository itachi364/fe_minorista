package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianTransportResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface DianTransportPort {

    DianTransportResult transmit(UUID submissionId, SubmitProviderDocumentCommand command,
            DianCompanyConfiguration configuration, DianSignedDocument signedDocument);
}
