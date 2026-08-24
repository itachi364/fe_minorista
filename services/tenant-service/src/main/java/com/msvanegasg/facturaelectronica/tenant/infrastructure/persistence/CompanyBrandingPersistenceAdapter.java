package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyBrandingRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyBranding;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyBrandingJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyBrandingJpaRepository;

@Component
public class CompanyBrandingPersistenceAdapter implements CompanyBrandingRepositoryPort {

    private final CompanyBrandingJpaRepository repository;

    public CompanyBrandingPersistenceAdapter(CompanyBrandingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyBranding save(CompanyBranding branding) {
        return toDomain(repository.save(toEntity(branding)));
    }

    @Override
    public Optional<CompanyBranding> findByCompanyId(UUID companyId) {
        return repository.findById(companyId).map(this::toDomain);
    }

    private CompanyBrandingJpaEntity toEntity(CompanyBranding branding) {
        return new CompanyBrandingJpaEntity(branding.companyId(), branding.displayName(), branding.primaryColor(),
                branding.accentColor(), branding.mainLogoStorageKey(), branding.headerLogoStorageKey(),
                branding.loginLogoStorageKey(), branding.faviconStorageKey(), branding.mainLogoContentType(),
                branding.headerLogoContentType(), branding.loginLogoContentType(), branding.faviconContentType(),
                branding.mainLogoHash(), branding.headerLogoHash(), branding.loginLogoHash(), branding.faviconHash(),
                branding.updatedBy(), branding.updatedAt());
    }

    private CompanyBranding toDomain(CompanyBrandingJpaEntity entity) {
        return new CompanyBranding(entity.getCompanyId(), entity.getDisplayName(), entity.getPrimaryColor(),
                entity.getAccentColor(), entity.getMainLogoStorageKey(), entity.getHeaderLogoStorageKey(),
                entity.getLoginLogoStorageKey(), entity.getFaviconStorageKey(), entity.getMainLogoContentType(),
                entity.getHeaderLogoContentType(), entity.getLoginLogoContentType(), entity.getFaviconContentType(),
                entity.getMainLogoHash(), entity.getHeaderLogoHash(), entity.getLoginLogoHash(),
                entity.getFaviconHash(), entity.getUpdatedBy(), entity.getUpdatedAt());
    }
}
