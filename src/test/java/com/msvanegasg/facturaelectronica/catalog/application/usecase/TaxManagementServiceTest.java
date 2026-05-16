package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.TaxCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.TaxRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoInactivoException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;

class TaxManagementServiceTest {

    @Test
    void createTaxStartsActiveAndUsesExistingCountry() {
        InMemoryTaxRepository taxRepository = new InMemoryTaxRepository();
        InMemoryCountryRepository countryRepository = new InMemoryCountryRepository();
        TaxManagementService service = new TaxManagementService(taxRepository, countryRepository);

        Tax tax = service.create(command("IVA", "CO"));

        assertThat(tax.id()).isEqualTo(1L);
        assertThat(tax.name()).isEqualTo("IVA");
        assertThat(tax.percentage()).isEqualByComparingTo("19.00");
        assertThat(tax.type()).isEqualTo("IVA");
        assertThat(tax.country().code()).isEqualTo("CO");
        assertThat(tax.active()).isTrue();
    }

    @Test
    void createTaxRejectsMissingCountry() {
        InMemoryTaxRepository taxRepository = new InMemoryTaxRepository();
        InMemoryCountryRepository countryRepository = new InMemoryCountryRepository();
        TaxManagementService service = new TaxManagementService(taxRepository, countryRepository);

        assertThatThrownBy(() -> service.create(command("IVA", "US")))
                .isInstanceOf(PaisNotFoundException.class);
    }

    @Test
    void updateTaxRejectsInactiveTax() {
        InMemoryTaxRepository taxRepository = new InMemoryTaxRepository();
        Country country = Country.restore("CO", "Colombia", "COP", true);
        taxRepository.save(Tax.restore(7L, "IVA", new BigDecimal("19.00"), "IVA", country, "Inicial", false));
        TaxManagementService service = new TaxManagementService(taxRepository, new InMemoryCountryRepository());

        assertThatThrownBy(() -> service.update(7L, command("IVA", "CO")))
                .isInstanceOf(ImpuestoInactivoException.class);
    }

    @Test
    void disableAndEnableTax() {
        InMemoryTaxRepository taxRepository = new InMemoryTaxRepository();
        Country country = Country.restore("CO", "Colombia", "COP", true);
        taxRepository.save(Tax.restore(7L, "IVA", new BigDecimal("19.00"), "IVA", country, "General", true));
        TaxManagementService service = new TaxManagementService(taxRepository, new InMemoryCountryRepository());

        service.disable(7L);
        assertThat(taxRepository.findById(7L).orElseThrow().active()).isFalse();

        service.enable(7L);
        assertThat(taxRepository.findById(7L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByIdRejectsMissingTax() {
        TaxManagementService service = new TaxManagementService(
                new InMemoryTaxRepository(),
                new InMemoryCountryRepository());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ImpuestoNotFoundException.class);
    }

    private static TaxCommand command(String name, String countryCode) {
        return new TaxCommand(name, new BigDecimal("19.00"), "IVA", countryCode, "Impuesto general");
    }

    private static final class InMemoryTaxRepository implements TaxRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Tax> taxes = new LinkedHashMap<>();

        @Override
        public List<Tax> findAll() {
            return List.copyOf(taxes.values());
        }

        @Override
        public Optional<Tax> findActive() {
            return taxes.values().stream().filter(Tax::active).findFirst();
        }

        @Override
        public Optional<Tax> findInactive() {
            return taxes.values().stream().filter(tax -> !tax.active()).findFirst();
        }

        @Override
        public Optional<Tax> findById(Long id) {
            return Optional.ofNullable(taxes.get(id));
        }

        @Override
        public Optional<Tax> findByPercentage(BigDecimal percentage) {
            return taxes.values().stream()
                    .filter(tax -> tax.percentage().compareTo(percentage) == 0)
                    .findFirst();
        }

        @Override
        public Optional<Tax> findByType(String type) {
            return taxes.values().stream()
                    .filter(tax -> tax.type().equals(type))
                    .findFirst();
        }

        @Override
        public Tax save(Tax tax) {
            Tax toSave = tax.id() == null
                    ? Tax.restore(nextId++, tax.name(), tax.percentage(), tax.type(), tax.country(),
                            tax.description(), tax.active())
                    : tax;
            taxes.put(toSave.id(), toSave);
            return toSave;
        }
    }

    private static final class InMemoryCountryRepository implements CountryRepositoryPort {

        private final Map<String, Country> countries = Map.of(
                "CO", Country.restore("CO", "Colombia", "COP", true));

        @Override
        public List<Country> findAll() {
            return List.copyOf(countries.values());
        }

        @Override
        public List<Country> findActive() {
            return countries.values().stream().filter(Country::active).toList();
        }

        @Override
        public List<Country> findInactive() {
            return countries.values().stream().filter(country -> !country.active()).toList();
        }

        @Override
        public Optional<Country> findByCode(String code) {
            return Optional.ofNullable(countries.get(code));
        }

        @Override
        public Country save(Country country) {
            throw new UnsupportedOperationException("not needed by this test");
        }
    }
}
