package com.msvanegasg.facturaelectronica.catalog.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCategoryUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCountryUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageDocumentTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageExpenseTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageParameterUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManagePaymentMethodUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageTaxUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.DocumentTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ExpenseTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.PaymentMethodRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.TaxRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.CategoryManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.CountryManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.DocumentTypeManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.ExpenseTypeManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.ParameterManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.PaymentMethodManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.ProductManagementService;
import com.msvanegasg.facturaelectronica.catalog.application.usecase.TaxManagementService;

@Configuration
public class CatalogUseCaseConfiguration {

    @Bean
    ManageCategoryUseCase manageCategoryUseCase(CategoryRepositoryPort categoryRepositoryPort) {
        return new CategoryManagementService(categoryRepositoryPort);
    }

    @Bean
    ManageCountryUseCase manageCountryUseCase(CountryRepositoryPort countryRepositoryPort) {
        return new CountryManagementService(countryRepositoryPort);
    }

    @Bean
    ManagePaymentMethodUseCase managePaymentMethodUseCase(PaymentMethodRepositoryPort paymentMethodRepositoryPort) {
        return new PaymentMethodManagementService(paymentMethodRepositoryPort);
    }

    @Bean
    ManageDocumentTypeUseCase manageDocumentTypeUseCase(DocumentTypeRepositoryPort documentTypeRepositoryPort) {
        return new DocumentTypeManagementService(documentTypeRepositoryPort);
    }

    @Bean
    ManageExpenseTypeUseCase manageExpenseTypeUseCase(ExpenseTypeRepositoryPort expenseTypeRepositoryPort) {
        return new ExpenseTypeManagementService(expenseTypeRepositoryPort);
    }

    @Bean
    ManageTaxUseCase manageTaxUseCase(TaxRepositoryPort taxRepositoryPort, CountryRepositoryPort countryRepositoryPort) {
        return new TaxManagementService(taxRepositoryPort, countryRepositoryPort);
    }

    @Bean
    ManageParameterUseCase manageParameterUseCase(ParameterRepositoryPort parameterRepositoryPort) {
        return new ParameterManagementService(parameterRepositoryPort);
    }

    @Bean
    ManageProductUseCase manageProductUseCase(ProductRepositoryPort productRepositoryPort,
            CategoryRepositoryPort categoryRepositoryPort) {
        return new ProductManagementService(productRepositoryPort, categoryRepositoryPort);
    }
}
