package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinResult;
import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageOperationalPinUseCase;
import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.OperationalPinRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.PasswordHasherPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.OperationalPin;

public class OperationalPinManagementService implements ManageOperationalPinUseCase {

    private final OperationalPinRepositoryPort repository;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;

    public OperationalPinManagementService(OperationalPinRepositoryPort repository, PasswordHasherPort passwordHasher,
            ClockPort clock) {
        this.repository = Objects.requireNonNull(repository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public OperationalPinResult configure(OperationalPinCommand command) {
        Objects.requireNonNull(command, "command is required");
        OperationalPin.validateRawPin(command.pin());
        OperationalPin pin = OperationalPin.configure(command.companyId(), passwordHasher.hash(command.pin()),
                clock.now());
        return toResult(repository.save(pin), true);
    }

    @Override
    public OperationalPinResult verify(OperationalPinCommand command) {
        Objects.requireNonNull(command, "command is required");
        OperationalPin.validateRawPin(command.pin());
        OperationalPin current = repository.findByCompanyId(command.companyId())
                .orElseThrow(() -> new IllegalStateException("Debes configurar el PIN operacional de la empresa."));
        if (current.locked() || current.mustChange()) {
            return toResult(current, false);
        }
        if (passwordHasher.matches(command.pin(), current.pinHash())) {
            return toResult(repository.save(current.registerSuccess(clock.now())), true);
        }
        return toResult(repository.save(current.registerFailedAttempt(clock.now())), false);
    }

    @Override
    public OperationalPinResult unlock(UUID companyId, String authorizationHeader) {
        OperationalPin current = repository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalStateException("Debes configurar el PIN operacional de la empresa."));
        return toResult(repository.save(current.unlockRequiringChange(clock.now())), false);
    }

    @Override
    public OperationalPinResult findStatus(UUID companyId, String authorizationHeader) {
        return repository.findByCompanyId(companyId)
                .map(pin -> toResult(pin, false))
                .orElseGet(() -> new OperationalPinResult(companyId, false, false, false, false, 3, null));
    }

    private static OperationalPinResult toResult(OperationalPin pin, boolean valid) {
        return new OperationalPinResult(pin.companyId(), true, valid, pin.locked(), pin.mustChange(),
                pin.remainingAttempts(), pin.updatedAt());
    }
}
