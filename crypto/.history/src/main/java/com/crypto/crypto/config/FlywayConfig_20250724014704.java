package com.crypto.crypto.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations}")
    private String flywayLocations;

    @Value("${spring.flyway.table}")
    private String flywayTable;

    @Bean
    @Profile("!test")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayLocations)
                .table(flywayTable)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(false) // Enable clean for development
                .load();
        
        return flyway;
    }

    @Bean
    @Profile("test")
    public Flyway flywayTest(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .table(flywayTable)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(false)
                .load();
        
        return flyway;
    }
} 