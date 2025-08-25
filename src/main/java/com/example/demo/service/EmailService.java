package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.subject.prefix:[ToDoApp]}")
    private String subjectPrefix;

    public void sendTaskDueNotification(String toEmail, String taskTitle, String dueDate) {
        if (!emailEnabled) {
            log.info("Email notifications are disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject(subjectPrefix + " Task Due Tomorrow: " + taskTitle);
            
            String emailBody = String.format(
                "Dear User,\n\n" +
                "This is a reminder that your task is due tomorrow:\n\n" +
                "Task: %s\n" +
                "Due Date: %s\n\n" +
                "Please complete this task on time.\n\n" +
                "Best regards,\n" +
                "ToDoApp Team",
                taskTitle, dueDate
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Task due notification sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send task due notification to {}: {}", toEmail, e.getMessage());
        }
    }
}