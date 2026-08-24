package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.Collection;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionArtifact;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEvent;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTechnicalValidationResult;

public interface DianSubmissionTraceRepositoryPort {

    void saveEvent(DianSubmissionEvent event);

    void saveValidationResults(Collection<DianTechnicalValidationResult> results);

    void saveArtifact(DianSubmissionArtifact artifact);
}
