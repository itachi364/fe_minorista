package com.msvanegasg.facturaelectronica.reporting.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportExportJobsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportDownloadAttemptPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportJobRepositoryPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportStoragePort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportNotificationPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.reporting.application.usecase.ReportExportJobService;
import com.msvanegasg.facturaelectronica.reporting.application.usecase.ReportManagementService;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.client.HttpReportDataGateway;

@Configuration
public class ReportingUseCaseConfiguration {

    @Bean
    ManageReportsUseCase manageReportsUseCase(ReportDataGateway dataGateway) {
        return new ReportManagementService(dataGateway);
    }

    @Bean
    ManageReportExportJobsUseCase manageReportExportJobsUseCase(ReportExportJobRepositoryPort repository,
            ReportExportDownloadAttemptPort downloadAttempts, ReportExportStoragePort storage,
            ReportNotificationPort notificationPort, TokenHashPort tokenHashPort, IdGeneratorPort idGenerator,
            ManageReportsUseCase reportsUseCase, ReportExportProperties properties, Clock clock) {
        return new ReportExportJobService(repository, downloadAttempts, storage, notificationPort, tokenHashPort,
                idGenerator, reportsUseCase, properties, clock);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ReportDataGateway reportDataGateway(RestClient.Builder builder, ReportingProperties properties,
            ObjectMapper objectMapper) {
        return new HttpReportDataGateway(builder, properties, objectMapper);
    }
}
