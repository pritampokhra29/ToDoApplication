package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.repo.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private EmailService emailService;

    /**
     * Scheduled method that runs daily at 9:00 AM to check for tasks due tomorrow
     * and send email notifications to users
     */
    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM
    public void checkTasksDueTomorrow() {
        log.info("Starting scheduled check for tasks due tomorrow");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        try {
            // Find all non-deleted tasks that are due tomorrow
            List<Task> tasksDueTomorrow = taskRepo.findByDueDateAndDeletedFalse(tomorrow);
            
            log.info("Found {} tasks due tomorrow ({})", tasksDueTomorrow.size(), tomorrow);
            
            for (Task task : tasksDueTomorrow) {
                try {
                    if (task.getUser() != null && task.getUser().getEmail() != null) {
                        emailService.sendTaskDueNotification(
                            task.getUser().getEmail(),
                            task.getTitle(),
                            task.getDueDate().toString()
                        );
                        
                        log.info("Notification sent for task '{}' to user '{}'", 
                            task.getTitle(), task.getUser().getEmail());
                    } else {
                        log.warn("Skipping notification for task '{}' - user or email is null", 
                            task.getTitle());
                    }
                } catch (Exception e) {
                    log.error("Failed to send notification for task '{}': {}", 
                        task.getTitle(), e.getMessage());
                }
            }
            
            log.info("Completed scheduled check for tasks due tomorrow");
            
        } catch (Exception e) {
            log.error("Error during scheduled task due check: {}", e.getMessage());
        }
    }

    /**
     * Manual method to trigger notification check (useful for testing)
     */
    public void manualCheckTasksDueTomorrow() {
        log.info("Manual trigger for tasks due tomorrow check");
        checkTasksDueTomorrow();
    }
}