package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration for production environment
 * Handles DATABASE_URL parsing for cloud platforms like Render, Heroku, etc.
 */
@Configuration
@Profile("prod")
public class ProductionDataSourceConfig {

    /**
     * Creates a DataSource from DATABASE_URL environment variable
     * Supports both JDBC and non-JDBC URL formats
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        try {
            // If it's already a JDBC URL, use it as-is
            if (databaseUrl.startsWith("jdbc:")) {
                return DataSourceBuilder.create()
                    .url(databaseUrl)
                    .driverClassName("org.postgresql.Driver")
                    .build();
            }

            // Parse non-JDBC URL (e.g., postgresql://user:pass@host:port/db)
            URI uri = new URI(databaseUrl);
            
            String scheme = uri.getScheme();
            if (!"postgresql".equals(scheme) && !"postgres".equals(scheme)) {
                throw new IllegalArgumentException("Unsupported database scheme: " + scheme);
            }
            
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String database = uri.getPath().substring(1); // Remove leading '/'
            
            // Build JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            // Extract credentials
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            
            if (userInfo != null) {
                String[] credentials = userInfo.split(":");
                username = credentials[0];
                password = credentials.length > 1 ? credentials[1] : "";
            }
            
            DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(jdbcUrl)
                .driverClassName("org.postgresql.Driver");
                
            if (username != null) {
                builder.username(username);
            }
            if (password != null) {
                builder.password(password);
            }
            
            return builder.build();
            
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid DATABASE_URL format: " + databaseUrl, e);
        }
    }
}
