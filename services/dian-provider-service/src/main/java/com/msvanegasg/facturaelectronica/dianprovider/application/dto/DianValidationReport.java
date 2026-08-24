package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.util.List;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTechnicalValidationResult;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianValidationStatus;

public record DianValidationReport(List<DianTechnicalValidationResult> results) {

    public boolean failed() {
        return results.stream().anyMatch(result -> result.result() == DianValidationStatus.FAILED);
    }

    public String firstFailureMessage() {
        return results.stream()
                .filter(result -> result.result() == DianValidationStatus.FAILED)
                .map(DianTechnicalValidationResult::message)
                .filter(message -> message != null && !message.isBlank())
                .findFirst()
                .orElse("La validacion tecnica DIAN fallo.");
    }
}
