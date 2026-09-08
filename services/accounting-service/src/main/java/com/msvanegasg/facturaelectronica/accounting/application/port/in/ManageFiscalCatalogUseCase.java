package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateWithholdingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;

public interface ManageFiscalCatalogUseCase {
    List<FiscalParameter> parameters();
    List<WithholdingRule> rules(UUID companyId, Boolean active);
    WithholdingRule create(CreateWithholdingRuleCommand command);
    WithholdingRule deactivate(UUID companyId, UUID ruleId);
}
