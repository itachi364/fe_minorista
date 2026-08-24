package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianValidationReport;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalValidationPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTechnicalValidationResult;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianValidationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianValidationType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;

@Component
public class BasicDianTechnicalValidationAdapter implements DianTechnicalValidationPort {

    private static final String SOURCE_VERSION = "DIAN-UBL-2.1-configured-artifacts";

    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public BasicDianTechnicalValidationAdapter(IdGeneratorPort idGenerator, ClockPort clock) {
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public DianValidationReport validate(UUID submissionId, SubmitProviderDocumentCommand command,
            DianCompanyConfiguration configuration, String unsignedXml, DianSignedDocument signedDocument,
            DianIdentifierResult identifiers) {
        Instant now = clock.now();
        List<DianTechnicalValidationResult> results = new ArrayList<>();
        results.add(result(command, submissionId, DianValidationType.XSD, rootMatches(command, unsignedXml),
                "UBL_ROOT", "El tipo documental no coincide con el nodo UBL esperado.", now));
        results.add(result(command, submissionId, DianValidationType.SCHEMATRON,
                unsignedXml != null && unsignedXml.contains("CustomizationID") && unsignedXml.contains("UBL 2.1"),
                "UBL_METADATA", "Falta metadata UBL/DIAN minima.", now));
        results.add(result(command, submissionId, DianValidationType.CODE_LIST,
                identifiers != null && hasText(identifiers.cufeCude()) && hasText(identifiers.qrContent()),
                "DIAN_IDENTIFIERS", "No se generaron CUFE/CUDE o QR.", now));
        results.add(result(command, submissionId, DianValidationType.SIGNATURE,
                signedDocument != null && hasText(signedDocument.xml()) && hasText(signedDocument.signatureDigest()),
                "XML_SIGNATURE", "No existe firma tecnica del XML.", now));
        return new DianValidationReport(results);
    }

    private DianTechnicalValidationResult result(SubmitProviderDocumentCommand command, UUID submissionId,
            DianValidationType type, boolean passed, String ruleCode, String failureMessage, Instant now) {
        return new DianTechnicalValidationResult(idGenerator.generate(), command.companyId(), submissionId,
                command.documentId(), type, passed ? DianValidationStatus.PASSED : DianValidationStatus.FAILED,
                ruleCode, passed ? "Validacion aprobada." : failureMessage, SOURCE_VERSION, now);
    }

    private static boolean rootMatches(SubmitProviderDocumentCommand command, String xml) {
        if (!hasText(xml)) {
            return false;
        }
        String expected = switch (command.documentType()) {
            case CREDIT_NOTE -> "<CreditNote";
            case DEBIT_NOTE -> "<DebitNote";
            case ELECTRONIC_INVOICE, ELECTRONIC_POS, POS_ADJUSTMENT_NOTE -> "<Invoice";
        };
        return xml.contains(expected);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
