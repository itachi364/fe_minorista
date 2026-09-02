package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileContentResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyFileAssetUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileAssetRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileAsset;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;

public class CompanyFileAssetManagementService implements ManageCompanyFileAssetUseCase {

    private static final long MAX_EVIDENCE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final CompanyRepositoryPort companyRepository;
    private final CompanyFileAssetRepositoryPort repository;
    private final CompanyFileStoragePort storage;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CompanyFileAssetManagementService(CompanyRepositoryPort companyRepository,
            CompanyFileAssetRepositoryPort repository, CompanyFileStoragePort storage, IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.companyRepository = Objects.requireNonNull(companyRepository);
        this.repository = Objects.requireNonNull(repository);
        this.storage = Objects.requireNonNull(storage);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public CompanyFileAssetResult upload(UUID companyId, CompanyFileAssetCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        validate(command);
        companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(companyId));
        UUID assetId = idGenerator.nextId();
        String hash = sha256(command.content());
        String storageKey = storageKey(companyId, command.category(), assetId, command.originalFilename());
        storage.save(storageKey, command.contentType(), command.content());
        CompanyFileAsset saved = repository.save(new CompanyFileAsset(assetId, companyId, command.category(),
                command.originalFilename(), command.contentType(), storageKey, command.content().length, hash,
                command.uploadedBy(), clock.now()));
        return toResult(saved);
    }

    @Override
    public CompanyFileContentResult read(UUID companyId, UUID assetId) {
        CompanyFileAsset asset = repository.findByCompanyIdAndId(companyId, assetId)
                .orElseThrow(() -> new CompanyFileAssetNotFoundException(assetId));
        return new CompanyFileContentResult(asset.originalFilename(), asset.contentType(), asset.contentHash(),
                storage.read(asset.storageKey()));
    }

    private static void validate(CompanyFileAssetCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.category(), "category is required");
        if (command.originalFilename() == null || command.originalFilename().isBlank()) {
            throw new IllegalArgumentException("originalFilename is required");
        }
        if (command.content() == null || command.content().length == 0) {
            throw new IllegalArgumentException("file is required");
        }
        if (command.content().length > MAX_EVIDENCE_SIZE_BYTES) {
            throw new IllegalArgumentException("file is too large");
        }
        if (isPdfOnly(command.category()) && !isPdf(command.originalFilename(), command.contentType())) {
            throw new IllegalArgumentException("Solo se permite PDF como evidencia documental.");
        }
    }

    private static boolean isPdfOnly(CompanyFileCategory category) {
        return category == CompanyFileCategory.PURCHASE_EVIDENCE || category == CompanyFileCategory.EXPENSE_EVIDENCE
                || category == CompanyFileCategory.INVOICE;
    }

    private static boolean isPdf(String filename, String contentType) {
        return filename.toLowerCase(Locale.ROOT).endsWith(".pdf")
                && "application/pdf".equalsIgnoreCase(contentType);
    }

    private static String storageKey(UUID companyId, CompanyFileCategory category, UUID assetId, String filename) {
        return companyId + "/" + category.folderName() + "/" + assetId + "-" + sanitize(filename);
    }

    private static String sanitize(String filename) {
        return filename.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static CompanyFileAssetResult toResult(CompanyFileAsset asset) {
        return new CompanyFileAssetResult(asset.id(), asset.companyId(), asset.category(), asset.originalFilename(),
                asset.contentType(), asset.fileSize(), asset.contentHash(),
                "/api/v1/companies/%s/files/%s?hash=%s".formatted(asset.companyId(), asset.id(),
                        asset.contentHash()),
                asset.uploadedBy(), asset.uploadedAt());
    }
}
