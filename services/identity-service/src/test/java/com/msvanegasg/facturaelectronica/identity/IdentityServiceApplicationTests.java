package com.msvanegasg.facturaelectronica.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyRoleRepositoryPort;

@SpringBootTest
class IdentityServiceApplicationTests {

    @Autowired
    private ManageIdentityUseCase manageIdentityUseCase;

    @Autowired
    private CompanyRoleRepositoryPort companyRoleRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void wiresPersistentCompanyRoleRepository() {
        assertThat(ReflectionTestUtils.getField(manageIdentityUseCase, "companyRoleRepository"))
                .isSameAs(companyRoleRepository);
    }
}
