package com.msvanegasg.facturaelectronica.reporting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOptionsResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryResult;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportDefinition;

public interface ManageReportsUseCase {

    List<ReportDefinition> definitions();

    ReportOptionsResult options(UUID companyId, String reportCode, String authorizationHeader);

    ReportQueryResult query(ReportQueryCommand command);

    ReportExportResult export(ReportQueryCommand command, ReportExportFormat format);
}
