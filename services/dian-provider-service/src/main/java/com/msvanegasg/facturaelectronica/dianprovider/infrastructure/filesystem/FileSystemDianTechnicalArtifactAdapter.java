package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalArtifactPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianConfigurationIncompleteException;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

@Component
public class FileSystemDianTechnicalArtifactAdapter implements DianTechnicalArtifactPort {

    private final DianProviderProperties properties;

    public FileSystemDianTechnicalArtifactAdapter(DianProviderProperties properties) {
        this.properties = properties;
    }

    @Override
    public void ensureReadyForRealMode() {
        Map<String, String> requiredArtifacts = new LinkedHashMap<>();
        requiredArtifacts.put("UBL invoice XSD", properties.ublInvoiceXsdPath());
        requiredArtifacts.put("UBL credit note XSD", properties.ublCreditNoteXsdPath());
        requiredArtifacts.put("UBL debit note XSD", properties.ublDebitNoteXsdPath());
        requiredArtifacts.put("DIAN model Schematron", properties.dianModelSchematronPath());
        requiredArtifacts.put("DIAN compiled XSL", properties.dianCompiledXslPath());
        requiredArtifacts.put("DIAN code list Schematron", properties.codeListSchematronPath());
        requiredArtifacts.forEach(this::ensureFileExists);
    }

    private void ensureFileExists(String name, String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new DianConfigurationIncompleteException("Falta configurar artefacto tecnico DIAN: " + name + ".");
        }
        Path path = resolve(configuredPath);
        if (!Files.isRegularFile(path)) {
            throw new DianConfigurationIncompleteException("No existe artefacto tecnico DIAN: " + name + ".");
        }
    }

    private Path resolve(String configuredPath) {
        Path path = Path.of(configuredPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        String root = properties.technicalArtifactsRoot();
        if (root == null || root.isBlank()) {
            return path.normalize();
        }
        return Path.of(root).resolve(path).normalize();
    }
}
