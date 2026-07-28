package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.identity.domain.model.UserSession;

public interface UserSessionRepositoryPort {

    UserSession save(UserSession session);

    Optional<UserSession> findByTokenHash(String tokenHash);
}
