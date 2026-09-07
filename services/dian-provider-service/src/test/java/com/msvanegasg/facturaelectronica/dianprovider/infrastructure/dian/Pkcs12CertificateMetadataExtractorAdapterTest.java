package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianInvalidCertificateException;

class Pkcs12CertificateMetadataExtractorAdapterTest {

    private final Pkcs12CertificateMetadataExtractorAdapter extractor = new Pkcs12CertificateMetadataExtractorAdapter();

    @Test
    void rejectsCertificateFileWithUnsupportedExtension() {
        assertThatThrownBy(() -> extractor.extract("certificado.pem", new byte[] { 1 }, "password"))
                .isInstanceOf(DianInvalidCertificateException.class)
                .hasMessageContaining(".p12 o .pfx");
    }

    @Test
    void rejectsEmptyCertificateFile() {
        assertThatThrownBy(() -> extractor.extract("certificado.p12", new byte[0], "password"))
                .isInstanceOf(DianInvalidCertificateException.class)
                .hasMessageContaining("vacio");
    }
}
