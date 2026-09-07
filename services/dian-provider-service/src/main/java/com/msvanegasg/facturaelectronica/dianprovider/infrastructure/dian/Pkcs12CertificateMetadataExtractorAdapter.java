package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.CertificateMetadata;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.CertificateMetadataExtractorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.usecase.DianInvalidCertificateException;

@Component
public class Pkcs12CertificateMetadataExtractorAdapter implements CertificateMetadataExtractorPort {

    private static final String P12_EXTENSION = ".p12";
    private static final String PFX_EXTENSION = ".pfx";

    @Override
    public CertificateMetadata extract(String fileName, byte[] content, String password) {
        validateInput(fileName, content, password);
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate x509Certificate) {
                    return new CertificateMetadata(alias, "sha256:" + sha256(certificate.getEncoded()),
                            x509Certificate.getNotAfter().toInstant());
                }
            }
            throw new DianInvalidCertificateException("El archivo .p12/.pfx no contiene un certificado X.509.");
        } catch (DianInvalidCertificateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DianInvalidCertificateException(
                    "No fue posible abrir el certificado .p12/.pfx con el password indicado.", exception);
        }
    }

    private static void validateInput(String fileName, byte[] content, String password) {
        String normalizedFileName = fileName == null ? "" : fileName.strip().toLowerCase(Locale.ROOT);
        if (!normalizedFileName.endsWith(P12_EXTENSION) && !normalizedFileName.endsWith(PFX_EXTENSION)) {
            throw new DianInvalidCertificateException("El certificado DIAN debe ser un archivo .p12 o .pfx.");
        }
        if (content == null || content.length == 0) {
            throw new DianInvalidCertificateException("El archivo de certificado DIAN esta vacio.");
        }
        if (password == null || password.isBlank()) {
            throw new DianInvalidCertificateException("El password del certificado DIAN es obligatorio.");
        }
    }

    private static String sha256(byte[] content) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
        StringBuilder builder = new StringBuilder();
        for (byte current : digest) {
            builder.append("%02x".formatted(current));
        }
        return builder.toString();
    }
}
