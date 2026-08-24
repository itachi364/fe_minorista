package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyBrandingUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.BrandingAssetStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyBrandingRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyBranding;

public class CompanyBrandingManagementService implements ManageCompanyBrandingUseCase {

    private static final int MAX_ASSET_BYTES = 1_048_576;
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp",
            "image/x-icon", "image/vnd.microsoft.icon");
    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp",
            "image/x-icon", "ico",
            "image/vnd.microsoft.icon", "ico");

    private final CompanyRepositoryPort companyRepository;
    private final CompanyBrandingRepositoryPort brandingRepository;
    private final BrandingAssetStoragePort storage;
    private final ClockPort clock;

    public CompanyBrandingManagementService(
            CompanyRepositoryPort companyRepository,
            CompanyBrandingRepositoryPort brandingRepository,
            BrandingAssetStoragePort storage,
            ClockPort clock) {
        this.companyRepository = companyRepository;
        this.brandingRepository = brandingRepository;
        this.storage = storage;
        this.clock = clock;
    }

    @Override
    public CompanyBrandingResult findByCompanyId(UUID companyId) {
        return CompanyBrandingResult.from(findOrEmpty(companyId));
    }

    @Override
    public CompanyBrandingResult update(UUID companyId, CompanyBrandingCommand command) {
        assertCompanyExists(companyId);
        validateColor(command.primaryColor());
        validateColor(command.accentColor());
        CompanyBranding branding = findOrEmpty(companyId)
                .updateMetadata(command.displayName(), command.primaryColor(), command.accentColor(),
                        command.updatedBy(), clock.now());
        return CompanyBrandingResult.from(brandingRepository.save(branding));
    }

    @Override
    public CompanyBrandingResult uploadAsset(UUID companyId, CompanyBrandingAssetCommand command) {
        assertCompanyExists(companyId);
        validateAsset(command);
        String contentHash = sha256(command.content());
        String storageKey = storageKey(companyId, command.purpose(), contentHash, command.contentType());
        storage.save(storageKey, command.content());
        CompanyBranding updated = findOrEmpty(companyId).updateAsset(command.purpose(), storageKey,
                command.contentType().toLowerCase(Locale.ROOT), contentHash, command.updatedBy(), clock.now());
        return CompanyBrandingResult.from(brandingRepository.save(updated));
    }

    @Override
    public CompanyBrandingAssetResult readAsset(UUID companyId, BrandingAssetPurpose purpose) {
        CompanyBranding branding = brandingRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new CompanyBrandingAssetNotFoundException(companyId, purpose));
        String storageKey = branding.storageKeyFor(purpose);
        String contentType = branding.contentTypeFor(purpose);
        if (storageKey == null || storageKey.isBlank() || contentType == null || contentType.isBlank()) {
            throw new CompanyBrandingAssetNotFoundException(companyId, purpose);
        }
        return new CompanyBrandingAssetResult(storage.read(storageKey), contentType, hashFor(branding, purpose));
    }

    private CompanyBranding findOrEmpty(UUID companyId) {
        assertCompanyExists(companyId);
        return brandingRepository.findByCompanyId(companyId).orElseGet(() -> CompanyBranding.empty(companyId, clock.now()));
    }

    private void assertCompanyExists(UUID companyId) {
        companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    private static void validateColor(String color) {
        if (color != null && !color.isBlank() && !HEX_COLOR.matcher(color.trim()).matches()) {
            throw new IllegalArgumentException("Invalid branding color");
        }
    }

    private static void validateAsset(CompanyBrandingAssetCommand command) {
        if (command.purpose() == null) {
            throw new IllegalArgumentException("Asset purpose is required");
        }
        if (command.content() == null || command.content().length == 0) {
            throw new IllegalArgumentException("Asset file is required");
        }
        if (command.content().length > MAX_ASSET_BYTES) {
            throw new IllegalArgumentException("Asset file is too large");
        }
        String contentType = normalizeContentType(command.contentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported asset content type");
        }
        String filename = command.originalFilename() == null ? "" : command.originalFilename().toLowerCase(Locale.ROOT);
        if (!filename.isBlank() && !extensionMatches(filename, contentType)) {
            throw new IllegalArgumentException("Asset extension does not match content type");
        }
    }

    private static boolean extensionMatches(String filename, String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return filename.endsWith(".jpg") || filename.endsWith(".jpeg");
        }
        return filename.endsWith("." + EXTENSIONS_BY_CONTENT_TYPE.get(contentType));
    }

    private static String storageKey(UUID companyId, BrandingAssetPurpose purpose, String hash, String contentType) {
        return "%s/%s/%s.%s".formatted(companyId, purpose.name().toLowerCase(Locale.ROOT), hash,
                EXTENSIONS_BY_CONTENT_TYPE.get(normalizeContentType(contentType)));
    }

    private static String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String hashFor(CompanyBranding branding, BrandingAssetPurpose purpose) {
        return switch (purpose) {
            case MAIN_LOGO -> branding.mainLogoHash();
            case HEADER_LOGO -> branding.headerLogoHash();
            case LOGIN_LOGO -> branding.loginLogoHash();
            case FAVICON -> branding.faviconHash();
        };
    }
}
