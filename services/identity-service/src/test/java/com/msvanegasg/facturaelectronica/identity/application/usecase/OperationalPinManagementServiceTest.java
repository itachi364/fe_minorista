package com.msvanegasg.facturaelectronica.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinCommand;
import com.msvanegasg.facturaelectronica.identity.application.port.out.OperationalPinRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.PasswordHasherPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.OperationalPin;

class OperationalPinManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-08-25T10:00:00Z");

    @Test
    void locksAfterThreeFailedAttemptsAndRequiresChangeAfterUnlock() {
        InMemoryOperationalPinRepository repository = new InMemoryOperationalPinRepository();
        OperationalPinManagementService service = new OperationalPinManagementService(repository,
                new PlainTextTestHasher(), () -> NOW);

        service.configure(new OperationalPinCommand(COMPANY_ID, "123456", "Bearer token"));

        assertThat(service.verify(new OperationalPinCommand(COMPANY_ID, "111111", "Bearer token")).valid()).isFalse();
        assertThat(service.verify(new OperationalPinCommand(COMPANY_ID, "222222", "Bearer token")).remainingAttempts())
                .isEqualTo(1);
        var locked = service.verify(new OperationalPinCommand(COMPANY_ID, "333333", "Bearer token"));

        assertThat(locked.locked()).isTrue();
        assertThat(locked.remainingAttempts()).isZero();
        assertThat(service.verify(new OperationalPinCommand(COMPANY_ID, "123456", "Bearer token")).valid()).isFalse();

        var unlocked = service.unlock(COMPANY_ID, "Bearer token");

        assertThat(unlocked.locked()).isFalse();
        assertThat(unlocked.mustChange()).isTrue();
    }

    @Test
    void validPinResetsFailedAttempts() {
        InMemoryOperationalPinRepository repository = new InMemoryOperationalPinRepository();
        OperationalPinManagementService service = new OperationalPinManagementService(repository,
                new PlainTextTestHasher(), () -> NOW);

        service.configure(new OperationalPinCommand(COMPANY_ID, "123456", "Bearer token"));
        service.verify(new OperationalPinCommand(COMPANY_ID, "111111", "Bearer token"));
        var result = service.verify(new OperationalPinCommand(COMPANY_ID, "123456", "Bearer token"));

        assertThat(result.valid()).isTrue();
        assertThat(result.remainingAttempts()).isEqualTo(3);
    }

    private static final class InMemoryOperationalPinRepository implements OperationalPinRepositoryPort {
        private final Map<UUID, OperationalPin> pins = new HashMap<>();

        @Override
        public OperationalPin save(OperationalPin operationalPin) {
            pins.put(operationalPin.companyId(), operationalPin);
            return operationalPin;
        }

        @Override
        public Optional<OperationalPin> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(pins.get(companyId));
        }
    }

    private static final class PlainTextTestHasher implements PasswordHasherPort {
        @Override
        public String hash(String rawPassword) {
            return rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return rawPassword.equals(encodedPassword);
        }
    }
}
