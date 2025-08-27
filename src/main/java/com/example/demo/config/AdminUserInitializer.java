package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminUserInitializer {
    // Note: This class is kept for potential future use
    // Currently using SQL scripts for data initialization

    // Commented out since we're using SQL scripts for initialization
    // @Bean
    // public CommandLineRunner createAdminUser() {
    //     return args -> {
    //         System.out.println("Executing AdminUserInitializer to create admin user...");
    //         if (userRepo.findByUsername("admin").isEmpty()) {
    //             User admin = new User();
    //             admin.setUsername("admin");
    //             admin.setEmail("admin@example.com");
    //             admin.setPassword(customPasswordEncoder.encode("admin123"));
    //             admin.setActive(true);
    //             admin.setRole("ADMIN"); // Set admin role
    //             userRepo.save(admin);
    //             System.out.println("Admin user created: admin / admin123");
    //         }
    //     };
    // }
}
