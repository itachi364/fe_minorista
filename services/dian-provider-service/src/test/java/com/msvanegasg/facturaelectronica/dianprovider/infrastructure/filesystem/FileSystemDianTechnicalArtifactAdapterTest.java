package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.filesystem;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianConfigurationIncompleteException;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

class FileSystemDianTechnicalArtifactAdapterTest {

    @TempDir
    private Path root;

    @Test
    void acceptsConfiguredTechnicalArtifacts() throws IOException {
        create("invoice.xsd");
        create("credit.xsd");
        create("debit.xsd");
        create("model.sch");
        create("compiled.xsl");
        create("codes.sch");

        new FileSystemDianTechnicalArtifactAdapter(properties()).ensureReadyForRealMode();
    }

    @Test
    void rejectsMissingTechnicalArtifact() throws IOException {
        create("invoice.xsd");

        FileSystemDianTechnicalArtifactAdapter adapter = new FileSystemDianTechnicalArtifactAdapter(properties());

        assertThatThrownBy(adapter::ensureReadyForRealMode)
                .isInstanceOf(DianConfigurationIncompleteException.class)
                .hasMessageContaining("artefacto tecnico DIAN");
    }

    private void create(String filename) throws IOException {
        Files.writeString(root.resolve(filename), "artifact");
    }

    private DianProviderProperties properties() {
        return new DianProviderProperties("mock", null, null, null, root.toString(), "invoice.xsd", "credit.xsd",
                "debit.xsd", "model.sch", "compiled.xsl", "codes.sch");
    }
}
