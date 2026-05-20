package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ParameterCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;
import com.msvanegasg.facturaelectronica.exception.ParametroNotFoundException;

class ParameterManagementServiceTest {

    @Test
    void createParameterStartsActiveAndPersistsIt() {
        InMemoryParameterRepository repository = new InMemoryParameterRepository();
        ParameterManagementService service = new ParameterManagementService(repository);

        Parameter parameter = service.create(new ParameterCommand("POS_PREFIX", "POS", "Prefijo POS"));

        assertThat(parameter.id()).isEqualTo(1L);
        assertThat(parameter.key()).isEqualTo("POS_PREFIX");
        assertThat(parameter.value()).isEqualTo("POS");
        assertThat(parameter.description()).isEqualTo("Prefijo POS");
        assertThat(parameter.active()).isTrue();
    }

    @Test
    void updateParameterKeepsIdentifierAndActiveState() {
        InMemoryParameterRepository repository = new InMemoryParameterRepository();
        repository.save(Parameter.restore(7L, "POS_PREFIX", "POS", "Inicial", true));
        ParameterManagementService service = new ParameterManagementService(repository);

        Parameter updated = service.update(7L, new ParameterCommand("POS_PREFIX", "P01", "Actualizado"));

        assertThat(updated.id()).isEqualTo(7L);
        assertThat(updated.key()).isEqualTo("POS_PREFIX");
        assertThat(updated.value()).isEqualTo("P01");
        assertThat(updated.active()).isTrue();
    }

    @Test
    void disableAndEnableParameter() {
        InMemoryParameterRepository repository = new InMemoryParameterRepository();
        repository.save(Parameter.restore(7L, "POS_PREFIX", "POS", null, true));
        ParameterManagementService service = new ParameterManagementService(repository);

        service.disable(7L);
        assertThat(repository.findById(7L).orElseThrow().active()).isFalse();

        service.enable(7L);
        assertThat(repository.findById(7L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByIdRejectsMissingParameter() {
        ParameterManagementService service = new ParameterManagementService(new InMemoryParameterRepository());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ParametroNotFoundException.class);
    }

    private static final class InMemoryParameterRepository implements ParameterRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Parameter> parameters = new LinkedHashMap<>();

        @Override
        public List<Parameter> findAll() {
            return List.copyOf(parameters.values());
        }

        @Override
        public List<Parameter> findActive() {
            return parameters.values().stream().filter(Parameter::active).toList();
        }

        @Override
        public List<Parameter> findInactive() {
            return parameters.values().stream().filter(parameter -> !parameter.active()).toList();
        }

        @Override
        public Optional<Parameter> findById(Long id) {
            return Optional.ofNullable(parameters.get(id));
        }

        @Override
        public Parameter save(Parameter parameter) {
            Parameter toSave = parameter.id() == null
                    ? Parameter.restore(nextId++, parameter.key(), parameter.value(), parameter.description(),
                            parameter.active())
                    : parameter;
            parameters.put(toSave.id(), toSave);
            return toSave;
        }
    }
}
