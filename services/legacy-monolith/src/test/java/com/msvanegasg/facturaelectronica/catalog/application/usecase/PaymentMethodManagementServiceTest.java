package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.PaymentMethodCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.PaymentMethodRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;
import com.msvanegasg.facturaelectronica.exception.MetodoPagoNotFoundException;

class PaymentMethodManagementServiceTest {

    @Test
    void createPaymentMethodStartsActiveAndPersistsIt() {
        InMemoryPaymentMethodRepository repository = new InMemoryPaymentMethodRepository();
        PaymentMethodManagementService service = new PaymentMethodManagementService(repository);

        PaymentMethod paymentMethod = service.create(new PaymentMethodCommand("Efectivo", "Pago en caja"));

        assertThat(paymentMethod.id()).isEqualTo(1L);
        assertThat(paymentMethod.name()).isEqualTo("Efectivo");
        assertThat(paymentMethod.description()).isEqualTo("Pago en caja");
        assertThat(paymentMethod.active()).isTrue();
    }

    @Test
    void updatePaymentMethodKeepsActiveState() {
        InMemoryPaymentMethodRepository repository = new InMemoryPaymentMethodRepository();
        PaymentMethod saved = repository.save(PaymentMethod.create("Efectivo", "Pago en caja"));
        PaymentMethodManagementService service = new PaymentMethodManagementService(repository);

        PaymentMethod updated = service.update(saved.id(), new PaymentMethodCommand("Tarjeta", "Pago con tarjeta"));

        assertThat(updated.id()).isEqualTo(saved.id());
        assertThat(updated.name()).isEqualTo("Tarjeta");
        assertThat(updated.active()).isTrue();
    }

    @Test
    void disableAndEnablePaymentMethod() {
        InMemoryPaymentMethodRepository repository = new InMemoryPaymentMethodRepository();
        PaymentMethod saved = repository.save(PaymentMethod.create("Efectivo", null));
        PaymentMethodManagementService service = new PaymentMethodManagementService(repository);

        service.disable(saved.id());
        assertThat(repository.findById(saved.id()).orElseThrow().active()).isFalse();

        service.enable(saved.id());
        assertThat(repository.findById(saved.id()).orElseThrow().active()).isTrue();
    }

    @Test
    void findByIdRejectsMissingPaymentMethod() {
        PaymentMethodManagementService service = new PaymentMethodManagementService(new InMemoryPaymentMethodRepository());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(MetodoPagoNotFoundException.class);
    }

    private static final class InMemoryPaymentMethodRepository implements PaymentMethodRepositoryPort {

        private final Map<Long, PaymentMethod> paymentMethods = new LinkedHashMap<>();
        private long sequence = 0L;

        @Override
        public List<PaymentMethod> findAll() {
            return List.copyOf(paymentMethods.values());
        }

        @Override
        public Optional<PaymentMethod> findActive() {
            return paymentMethods.values().stream()
                    .filter(PaymentMethod::active)
                    .findFirst();
        }

        @Override
        public Optional<PaymentMethod> findInactive() {
            return paymentMethods.values().stream()
                    .filter(paymentMethod -> !paymentMethod.active())
                    .findFirst();
        }

        @Override
        public Optional<PaymentMethod> findById(Long id) {
            return Optional.ofNullable(paymentMethods.get(id));
        }

        @Override
        public PaymentMethod save(PaymentMethod paymentMethod) {
            PaymentMethod toSave = paymentMethod.id() == null
                    ? PaymentMethod.restore(++sequence, paymentMethod.name(), paymentMethod.description(), paymentMethod.active())
                    : paymentMethod;
            paymentMethods.put(toSave.id(), toSave);
            return toSave;
        }
    }
}
