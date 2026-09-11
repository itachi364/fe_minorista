package com.msvanegasg.facturaelectronica.accountingservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class AccountingServiceApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(jdbcTemplate.queryForObject("SELECT current_schema()", String.class)).isEqualTo("accounting");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM national_fiscal_concept_catalog", Integer.class))
                .isGreaterThan(0);
    }
}
