package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;

public interface UserAccountRepositoryPort {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findById(UUID id);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
