package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianIdentifierCalculationPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.CudeHashGenerator;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

@Component
public class Sha256DianIdentifierCalculationAdapter implements DianIdentifierCalculationPort {

    @Override
    public DianIdentifierResult calculate(SubmitProviderDocumentCommand command, DianCompanyConfiguration configuration,
            String unsignedXml) {
        String source = command.companyId() + "|" + command.documentId() + "|" + command.documentType() + "|"
                + configuration.environment() + "|" + configuration.softwareId() + "|"
                + configuration.technicalKeySecretRef() + "|" + unsignedXml;
        String identifier = CudeHashGenerator.generate(source);
        String qr = "NumDoc=" + command.documentId() + "&Tipo=" + command.documentType() + "&Ambiente="
                + configuration.environment() + "&CUFE_CUDE=" + identifier;
        return new DianIdentifierResult(identifier, qr);
    }
}
