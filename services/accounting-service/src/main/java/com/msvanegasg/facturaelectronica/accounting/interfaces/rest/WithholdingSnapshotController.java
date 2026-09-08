package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryWithholdingSnapshotsUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.WithholdingSnapshotResponse;

@RestController
@RequestMapping("/api/v1/fiscal-calculations/withholdings/snapshots")
public class WithholdingSnapshotController {
    private final QueryWithholdingSnapshotsUseCase useCase;

    public WithholdingSnapshotController(QueryWithholdingSnapshotsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<WithholdingSnapshotResponse> find(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam AccountingSourceType sourceType, @RequestParam UUID sourceId) {
        return useCase.find(companyId, sourceType, sourceId).stream()
                .map(WithholdingSnapshotController::toResponse).toList();
    }

    private static WithholdingSnapshotResponse toResponse(WithholdingCalculationSnapshot item) {
        return new WithholdingSnapshotResponse(item.id(), item.sourceType(), item.sourceId(), item.thirdPartyId(),
                item.operationDate(), item.withholdingType(), item.baseAmount(), item.rate(), item.amount(),
                item.ruleVersion(), item.decision(), item.reason(), item.ruleId(), item.parameterVersion(),
                item.legalReference(), item.sourceUrl(), item.createdAt());
    }
}
