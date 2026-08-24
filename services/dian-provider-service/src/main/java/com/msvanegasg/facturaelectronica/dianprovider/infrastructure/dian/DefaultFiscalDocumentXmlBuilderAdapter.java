package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianXmlDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalDocumentXmlBuilderPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;

@Component
public class DefaultFiscalDocumentXmlBuilderAdapter implements FiscalDocumentXmlBuilderPort {

    @Override
    public DianXmlDocument build(SubmitProviderDocumentCommand command, DianCompanyConfiguration configuration) {
        String root = rootName(command.documentType());
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<" + root + " xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:" + root + "-2\""
                + " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\""
                + " xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\">"
                + "<cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>"
                + "<cbc:CustomizationID>DIAN UBL 2.1</cbc:CustomizationID>"
                + "<cbc:ProfileExecutionID>" + escape(configuration.environment().name()) + "</cbc:ProfileExecutionID>"
                + "<cbc:ID>" + command.documentId() + "</cbc:ID>"
                + "<cbc:UUID schemeName=\"NexoFiscalPending\">" + command.idempotencyKey() + "</cbc:UUID>"
                + "<cbc:DocumentTypeCode>" + command.documentType() + "</cbc:DocumentTypeCode>"
                + "<cac:AccountingSupplierParty><cbc:AdditionalAccountID>" + command.companyId()
                + "</cbc:AdditionalAccountID></cac:AccountingSupplierParty>"
                + "<cac:AdditionalDocumentReference><cbc:ID>" + command.documentId()
                + "</cbc:ID><cbc:DocumentDescription><![CDATA[" + safePayload(command.payload())
                + "]]></cbc:DocumentDescription></cac:AdditionalDocumentReference>"
                + "</" + root + ">";
        return new DianXmlDocument(xml, command.documentType().name().toLowerCase() + "-" + command.documentId()
                + ".xml");
    }

    private static String rootName(ProviderDocumentType documentType) {
        return switch (documentType) {
            case CREDIT_NOTE -> "CreditNote";
            case DEBIT_NOTE -> "DebitNote";
            case ELECTRONIC_INVOICE, ELECTRONIC_POS, POS_ADJUSTMENT_NOTE -> "Invoice";
        };
    }

    private static String safePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "{}";
        }
        return payload.replace("]]>", "");
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
