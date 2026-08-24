package com.msvanegasg.facturaelectronica.reporting.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.application.usecase.ReportManagementService;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.client.HttpReportDataGateway;

@Configuration
public class ReportingUseCaseConfiguration {

    @Bean
    ManageReportsUseCase manageReportsUseCase(ReportDataGateway dataGateway) {
        return new ReportManagementService(dataGateway);
    }

    @Bean
    ReportDataGateway reportDataGateway(RestClient.Builder builder, ReportingProperties properties,
            ObjectMapper objectMapper) {
        return new HttpReportDataGateway(builder, properties, objectMapper);
    }
}
