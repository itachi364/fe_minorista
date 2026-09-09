package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class LicensePlanPolicy {

    public static final String POS = "POS";
    public static final String FULL = "FULL";
    public static final String CUSTOM = "CUSTOM";

    private static final Set<LicenseModule> POS_MODULES = Set.of(
            LicenseModule.COMPANY,
            LicenseModule.THIRDPARTY,
            LicenseModule.INVENTORY,
            LicenseModule.BILLING,
            LicenseModule.REPORTS,
            LicenseModule.CATALOGS,
            LicenseModule.AUDIT,
            LicenseModule.USERS);

    private static final Set<LicenseFeature> POS_FEATURES = Set.of(
            LicenseFeature.COMPANY_BASIC,
            LicenseFeature.BRANDING_BASIC,
            LicenseFeature.CUSTOMERS,
            LicenseFeature.PRODUCTS_SERVICES,
            LicenseFeature.INVENTORY_BASIC,
            LicenseFeature.POS_SALES,
            LicenseFeature.ELECTRONIC_BILLING,
            LicenseFeature.SALES_REGISTRY,
            LicenseFeature.FISCAL_SETTINGS_BASIC,
            LicenseFeature.FISCAL_DOCUMENTS_BASIC,
            LicenseFeature.REPORTS_BASIC,
            LicenseFeature.CATALOGS_OPERATING,
            LicenseFeature.AUDIT_BASIC,
            LicenseFeature.USERS_BASIC,
            LicenseFeature.OPERATIONAL_PIN,
            LicenseFeature.ACCOUNTING_CORE);

    private LicensePlanPolicy() {
    }

    public static Selection normalize(String planCode, Set<LicenseModule> modules, Set<LicenseFeature> features) {
        String normalizedPlan = normalizePlanCode(planCode);
        if (POS.equals(normalizedPlan)) {
            return new Selection(POS, POS_MODULES, POS_FEATURES);
        }
        if (FULL.equals(normalizedPlan)) {
            return new Selection(FULL, EnumSet.allOf(LicenseModule.class), standardFeatures());
        }
        Set<LicenseModule> customModules = immutableModules(modules);
        Set<LicenseFeature> customFeatures = immutableFeatures(features);
        for (LicenseFeature feature : customFeatures) {
            if (feature.module() != null && !customModules.contains(feature.module())) {
                throw new IllegalArgumentException("feature " + feature + " requires module " + feature.module());
            }
        }
        return new Selection(CUSTOM, customModules, customFeatures);
    }

    public static Set<LicenseModule> posModules() {
        return POS_MODULES;
    }

    public static Set<LicenseFeature> posFeatures() {
        return POS_FEATURES;
    }

    public static Set<LicenseFeature> standardFeatures() {
        EnumSet<LicenseFeature> features = EnumSet.noneOf(LicenseFeature.class);
        for (LicenseFeature feature : LicenseFeature.values()) {
            if (feature.standard()) {
                features.add(feature);
            }
        }
        return Set.copyOf(features);
    }

    private static String normalizePlanCode(String planCode) {
        if (planCode == null || planCode.isBlank()) {
            throw new IllegalArgumentException("planCode is required");
        }
        String value = planCode.trim().toUpperCase(Locale.ROOT);
        return POS.equals(value) || FULL.equals(value) ? value : CUSTOM;
    }

    private static Set<LicenseModule> immutableModules(Set<LicenseModule> modules) {
        return modules == null || modules.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(modules));
    }

    private static Set<LicenseFeature> immutableFeatures(Set<LicenseFeature> features) {
        return features == null || features.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(features));
    }

    public record Selection(String planCode, Set<LicenseModule> modules, Set<LicenseFeature> features) {
    }
}
