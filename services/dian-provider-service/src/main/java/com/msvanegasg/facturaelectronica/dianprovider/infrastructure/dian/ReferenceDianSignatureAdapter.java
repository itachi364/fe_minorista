package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSignaturePort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.CudeHashGenerator;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

@Component
public class ReferenceDianSignatureAdapter implements DianSignaturePort {

    @Override
    public DianSignedDocument sign(DianCompanyConfiguration configuration, String unsignedXml,
            DianIdentifierResult identifiers) {
        String digest = CudeHashGenerator.generate(unsignedXml + "|" + identifiers.cufeCude() + "|"
                + configuration.certificateFingerprint() + "|" + configuration.certificateSecretRef());
        String marker = "<!--NEXOFISCAL-SIGNATURE-DIGEST:" + digest + "-->";
        int closing = unsignedXml.lastIndexOf("</");
        String signedXml = closing < 0 ? unsignedXml + marker : unsignedXml.substring(0, closing) + marker
                + unsignedXml.substring(closing);
        return new DianSignedDocument(signedXml, digest);
    }
}
