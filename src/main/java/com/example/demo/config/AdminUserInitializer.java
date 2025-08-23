package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entity.User;
import com.example.demo.repo.UserRepo;

@Configuration
public class AdminUserInitializer {
    @Autowired
    private UserRepo userRepo;

    @Autowired
	CustomPasswordEncoder customPasswordEncoder;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            System.out.println("Executing AdminUserInitializer to create admin user...");
//            System.out.println("Result of findByUsername('admin'): " + userRepo.findByUsername("admin"));
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(customPasswordEncoder.encode("admin123"));
                admin.setActive(true);
                admin.setRole("ADMIN"); // Set admin role
                userRepo.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }
        };
    }
}
