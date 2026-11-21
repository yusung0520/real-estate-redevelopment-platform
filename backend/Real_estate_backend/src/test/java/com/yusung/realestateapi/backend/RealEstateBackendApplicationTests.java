package com.yusung.realestateapi.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RealEstateBackendApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
    }

    @Test
    void usesIsolatedInMemoryDatabase() {
        assertThat(environment.getProperty("spring.datasource.url"))
                .startsWith("jdbc:h2:mem:");
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                .isEqualTo("none");
        assertThat(environment.getProperty("spring.sql.init.mode"))
                .isEqualTo("never");
        assertThat(environment.getProperty("pipeline.cron"))
                .isEqualTo("-");
    }

}
