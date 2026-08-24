package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;

public interface UserAccountRepositoryPort {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findById(UUID id);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByCognitoSubject(String cognitoSubject);

    List<UserAccount> findByCompanyIdAndEmailContaining(UUID companyId, String email);

    long countByCompanyId(UUID companyId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
