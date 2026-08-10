package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FinalConsumerProfile;

public interface FinalConsumerProfileRepositoryPort {

    Optional<FinalConsumerProfile> findActiveForCompanyOrGlobal(UUID companyId);
}
