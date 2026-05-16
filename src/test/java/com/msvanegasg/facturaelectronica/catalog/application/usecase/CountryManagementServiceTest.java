package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CountryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;

class CountryManagementServiceTest {

    @Test
    void createCountryStartsActiveAndPersistsIt() {
        InMemoryCountryRepository repository = new InMemoryCountryRepository();
        CountryManagementService service = new CountryManagementService(repository);

        Country country = service.create(new CountryCommand("CO", "Colombia", "COP"));

        assertThat(country.code()).isEqualTo("CO");
        assertThat(country.name()).isEqualTo("Colombia");
        assertThat(country.currency()).isEqualTo("COP");
        assertThat(country.active()).isTrue();
        assertThat(repository.findByCode("CO")).isPresent();
    }

    @Test
    void updateCountryKeepsCodeAndActiveState() {
        InMemoryCountryRepository repository = new InMemoryCountryRepository();
        repository.save(Country.restore("CO", "Colombia", "COP", true));
        CountryManagementService service = new CountryManagementService(repository);

        Country country = service.update("CO", new CountryCommand("US", "Republica de Colombia", "COP"));

        assertThat(country.code()).isEqualTo("CO");
        assertThat(country.name()).isEqualTo("Republica de Colombia");
        assertThat(country.active()).isTrue();
    }

    @Test
    void disableAndEnableCountry() {
        InMemoryCountryRepository repository = new InMemoryCountryRepository();
        repository.save(Country.restore("CO", "Colombia", "COP", true));
        CountryManagementService service = new CountryManagementService(repository);

        service.disable("CO");
        assertThat(repository.findByCode("CO").orElseThrow().active()).isFalse();

        service.enable("CO");
        assertThat(repository.findByCode("CO").orElseThrow().active()).isTrue();
    }

    @Test
    void findByCodeRejectsMissingCountry() {
        CountryManagementService service = new CountryManagementService(new InMemoryCountryRepository());

        assertThatThrownBy(() -> service.findByCode("CO"))
                .isInstanceOf(PaisNotFoundException.class);
    }

    private static final class InMemoryCountryRepository implements CountryRepositoryPort {

        private final Map<String, Country> countries = new LinkedHashMap<>();

        @Override
        public List<Country> findAll() {
            return List.copyOf(countries.values());
        }

        @Override
        public List<Country> findActive() {
            return countries.values().stream()
                    .filter(Country::active)
                    .toList();
        }

        @Override
        public List<Country> findInactive() {
            return countries.values().stream()
                    .filter(country -> !country.active())
                    .toList();
        }

        @Override
        public Optional<Country> findByCode(String code) {
            return Optional.ofNullable(countries.get(code));
        }

        @Override
        public Country save(Country country) {
            countries.put(country.code(), country);
            return country;
        }
    }
}
