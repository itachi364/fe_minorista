package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

@ConfigurationProperties(prefix = "dian.mock")
public class DianMockProviderProperties {

    private ProviderSubmissionStatus defaultStatus = ProviderSubmissionStatus.ACCEPTED;
    private String errorCode;
    private String errorMessage;

    public ProviderSubmissionStatus getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(ProviderSubmissionStatus defaultStatus) {
        this.defaultStatus = defaultStatus == null ? ProviderSubmissionStatus.ACCEPTED : defaultStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
