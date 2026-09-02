package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileAssetRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileAsset;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyFileAssetJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyFileAssetJpaRepository;

@Component
public class CompanyFileAssetPersistenceAdapter implements CompanyFileAssetRepositoryPort {

    private final CompanyFileAssetJpaRepository repository;

    public CompanyFileAssetPersistenceAdapter(CompanyFileAssetJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyFileAsset save(CompanyFileAsset asset) {
        return toDomain(repository.save(toEntity(asset)));
    }

    @Override
    public Optional<CompanyFileAsset> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(this::toDomain);
    }

    private CompanyFileAssetJpaEntity toEntity(CompanyFileAsset asset) {
        CompanyFileAssetJpaEntity entity = new CompanyFileAssetJpaEntity();
        entity.setId(asset.id());
        entity.setCompanyId(asset.companyId());
        entity.setCategory(asset.category());
        entity.setOriginalFilename(asset.originalFilename());
        entity.setContentType(asset.contentType());
        entity.setStorageKey(asset.storageKey());
        entity.setFileSize(asset.fileSize());
        entity.setContentHash(asset.contentHash());
        entity.setUploadedBy(asset.uploadedBy());
        entity.setUploadedAt(asset.uploadedAt());
        return entity;
    }

    private CompanyFileAsset toDomain(CompanyFileAssetJpaEntity entity) {
        return new CompanyFileAsset(entity.getId(), entity.getCompanyId(), entity.getCategory(),
                entity.getOriginalFilename(), entity.getContentType(), entity.getStorageKey(), entity.getFileSize(),
                entity.getContentHash(), entity.getUploadedBy(), entity.getUploadedAt());
    }
}
