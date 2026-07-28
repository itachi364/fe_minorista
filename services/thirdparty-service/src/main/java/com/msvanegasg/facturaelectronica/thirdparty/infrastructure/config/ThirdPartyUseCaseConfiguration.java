package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageThirdPartyUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.ThirdPartyRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.usecase.ThirdPartyManagementService;

@Configuration("thirdPartyCleanUseCaseConfiguration")
public class ThirdPartyUseCaseConfiguration {

    @Bean
    ManageThirdPartyUseCase manageThirdPartyUseCase(ThirdPartyRepositoryPort thirdPartyRepositoryPort) {
        return new ThirdPartyManagementService(thirdPartyRepositoryPort);
    }
}