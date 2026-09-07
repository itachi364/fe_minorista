package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.CertificateMetadata;

public interface CertificateMetadataExtractorPort {

    CertificateMetadata extract(String fileName, byte[] content, String password);
}
