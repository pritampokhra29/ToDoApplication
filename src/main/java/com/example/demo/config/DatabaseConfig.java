package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuration to handle DATABASE_URL conversion for production
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        String databaseUrl = System.getenv("DATABASE_URL");
        logger.info("Original DATABASE_URL format detected: {}", 
            databaseUrl != null ? databaseUrl.substring(0, Math.min(20, databaseUrl.length())) + "..." : "null");
        
        if (databaseUrl != null && !databaseUrl.startsWith("jdbc:")) {
            // Convert postgresql:// to jdbc:postgresql://
            if (databaseUrl.startsWith("postgresql://")) {
                databaseUrl = "jdbc:" + databaseUrl;
                logger.info("Converted DATABASE_URL to JDBC format");
            } else if (databaseUrl.startsWith("postgres://")) {
                databaseUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
                logger.info("Converted postgres:// to jdbc:postgresql://");
            }
        } else if (databaseUrl != null) {
            logger.info("DATABASE_URL already in JDBC format");
        }
        
        properties.setUrl(databaseUrl);
        properties.setDriverClassName("org.postgresql.Driver");
        
        logger.info("DataSource configuration completed");
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        logger.info("Creating DataSource bean");
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }
}
