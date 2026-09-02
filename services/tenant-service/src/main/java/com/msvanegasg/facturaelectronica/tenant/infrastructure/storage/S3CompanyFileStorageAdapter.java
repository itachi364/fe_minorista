package com.msvanegasg.facturaelectronica.tenant.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileStoragePort;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

@Component
@ConditionalOnProperty(name = "tenant.files.storage-provider", havingValue = "s3")
public class S3CompanyFileStorageAdapter implements CompanyFileStoragePort {

    private final S3Client s3Client;
    private final String bucket;
    private final String kmsKeyId;

    public S3CompanyFileStorageAdapter(@Value("${tenant.files.s3.bucket}") String bucket,
            @Value("${tenant.files.s3.region}") String region,
            @Value("${tenant.files.s3.kms-key-id:}") String kmsKeyId) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("tenant.files.s3.bucket is required when S3 storage is enabled");
        }
        this.bucket = bucket;
        this.kmsKeyId = kmsKeyId;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Override
    public void save(String storageKey, String contentType, byte[] content) {
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(contentType)
                .contentLength((long) content.length);
        if (kmsKeyId != null && !kmsKeyId.isBlank()) {
            request.serverSideEncryption(ServerSideEncryption.AWS_KMS).ssekmsKeyId(kmsKeyId);
        } else {
            request.serverSideEncryption(ServerSideEncryption.AES256);
        }
        s3Client.putObject(request.build(), RequestBody.fromBytes(content));
    }

    @Override
    public byte[] read(String storageKey) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build());
        return response.asByteArray();
    }
}
