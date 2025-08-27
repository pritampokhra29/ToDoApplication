package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Early environment configuration to handle DATABASE_URL conversion
 */
@Component
public class DatabaseUrlConverter implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUrlConverter.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        // Only process if prod profile is active
        if (!environment.acceptsProfiles("prod")) {
            return;
        }
        
        String databaseUrl = environment.getProperty("DATABASE_URL");
        logger.info("Processing DATABASE_URL in early environment preparation: {}", databaseUrl);
        
        if (databaseUrl != null && (databaseUrl.startsWith("postgresql://") || databaseUrl.startsWith("postgres://"))) {
            try {
                // Parse the DATABASE_URL
                String withoutProtocol = databaseUrl.replaceFirst("^postgres(ql)?://", "");
                String[] userHostSplit = withoutProtocol.split("@", 2);
                
                if (userHostSplit.length == 2) {
                    String userPassword = userHostSplit[0];
                    String hostPortDb = userHostSplit[1];
                    
                    String[] userPassSplit = userPassword.split(":", 2);
                    String username = userPassSplit[0];
                    String password = userPassSplit.length > 1 ? userPassSplit[1] : "";
                    
                    String[] hostDbSplit = hostPortDb.split("/", 2);
                    String hostPort = hostDbSplit[0];
                    String database = hostDbSplit.length > 1 ? hostDbSplit[1] : "";
                    
                    // Create proper JDBC URL
                    String jdbcUrl = "jdbc:postgresql://" + hostPort + "/" + database;
                    
                    logger.info("Converting DATABASE_URL - Host: {}, Database: {}, User: {}", hostPort, database, username);
                    logger.info("New JDBC URL: {}", jdbcUrl);
                    
                    // Create a map with the converted properties
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("spring.datasource.url", jdbcUrl);
                    properties.put("spring.datasource.username", username);
                    properties.put("spring.datasource.password", password);
                    properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
                    
                    // IMPORTANT: Remove the original DATABASE_URL to prevent conflicts
                    Map<String, Object> clearProperties = new HashMap<>();
                    clearProperties.put("DATABASE_URL", null);
                    
                    // Add the clear properties first
                    MapPropertySource clearSource = new MapPropertySource("clear-database-url", clearProperties);
                    environment.getPropertySources().addFirst(clearSource);
                    
                    // Then add the converted properties with highest priority
                    MapPropertySource propertySource = new MapPropertySource("converted-database-url", properties);
                    environment.getPropertySources().addFirst(propertySource);
                    
                    logger.info("Successfully converted DATABASE_URL to Spring datasource properties and cleared original URL");
                } else {
                    logger.warn("Invalid DATABASE_URL format: {}", databaseUrl);
                }
            } catch (Exception e) {
                logger.error("Error converting DATABASE_URL: {}", e.getMessage(), e);
            }
        }
    }
}
