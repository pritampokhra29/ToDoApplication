package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Manual trigger for checking tasks due tomorrow - useful for testing
     * Only admins can trigger this
     */
    @PostMapping("/check-due-tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerDueTaskCheck() {
        log.info("Manual trigger for due task notifications requested");
        
        try {
            notificationService.manualCheckTasksDueTomorrow();
            return ResponseEntity.ok("Task due notifications check completed successfully");
        } catch (Exception e) {
            log.error("Error during manual task due check: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body("Error occurred during task due check: " + e.getMessage());
        }
    }
}