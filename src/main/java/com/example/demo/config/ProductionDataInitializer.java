package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.entity.Task;
import com.example.demo.repo.UserRepo;
import com.example.demo.repo.TaskRepo;
import com.example.demo.config.CustomPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Production data initializer
 * Only runs in production profile when app.init.production-data=true
 */
@Component
@Profile("prod")
public class ProductionDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductionDataInitializer.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private TaskRepo taskRepository;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Value("${app.init.production-data:false}")
    private boolean initProductionData;

    @Override
    public void run(String... args) throws Exception {
        if (!initProductionData) {
            logger.info("Production data initialization is disabled");
            return;
        }

        logger.info("Starting production data initialization...");

        try {
            // Create admin user if not exists
            if (!userRepository.findByUsername("admin").isPresent()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // This will use production pepper
                admin.setEmail("admin@todolist.com");
                admin.setRole("ADMIN");
                admin.setActive(true);
                admin.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                admin.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                
                userRepository.save(admin);
                logger.info("✅ Created admin user for production");

                // Create welcome task for admin
                Task welcomeTask = new Task();
                welcomeTask.setTitle("Welcome to ToDoList!");
                welcomeTask.setDescription("This is your first task. Feel free to edit or delete it.");
                welcomeTask.setDueDate(LocalDate.now().plusDays(7));
                welcomeTask.setStatus(com.example.demo.constants.Status.PENDING);
                welcomeTask.setCreateDate(LocalDate.now());
                welcomeTask.setUpdateDate(LocalDate.now());
                welcomeTask.setDeleted(false);
                welcomeTask.setCategory("Welcome");
                welcomeTask.setPriority(com.example.demo.constants.Priority.LOW);
                welcomeTask.setUser(admin);

                taskRepository.save(welcomeTask);
                logger.info("✅ Created welcome task for admin");
            } else {
                logger.info("Admin user already exists, skipping creation");
            }

            // Create test user if not exists
            if (!userRepository.findByUsername("user").isPresent()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password123")); // This will use production pepper
                user.setEmail("user@todolist.com");
                user.setRole("USER");
                user.setActive(true);
                user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                
                userRepository.save(user);
                logger.info("✅ Created test user for production");
            } else {
                logger.info("Test user already exists, skipping creation");
            }

            logger.info("✅ Production data initialization completed successfully");

        } catch (Exception e) {
            logger.error("❌ Error during production data initialization: {}", e.getMessage(), e);
            throw e;
        }
    }
}
