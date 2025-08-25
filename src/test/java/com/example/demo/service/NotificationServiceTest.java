package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repo.TaskRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private TaskRepo taskRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testCheckTasksDueTomorrow_WithValidTasks() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        
        Task task1 = new Task();
        task1.setTitle("Test Task 1");
        task1.setDueDate(tomorrow);
        task1.setUser(user);
        task1.setDeleted(false);
        
        Task task2 = new Task();
        task2.setTitle("Test Task 2");
        task2.setDueDate(tomorrow);
        task2.setUser(user);
        task2.setDeleted(false);
        
        when(taskRepo.findByDueDateAndDeletedFalse(tomorrow))
            .thenReturn(Arrays.asList(task1, task2));

        // Act
        notificationService.manualCheckTasksDueTomorrow();

        // Assert
        verify(taskRepo).findByDueDateAndDeletedFalse(tomorrow);
        verify(emailService).sendTaskDueNotification(
            eq("test@example.com"), 
            eq("Test Task 1"), 
            eq(tomorrow.toString())
        );
        verify(emailService).sendTaskDueNotification(
            eq("test@example.com"), 
            eq("Test Task 2"), 
            eq(tomorrow.toString())
        );
    }

    @Test
    void testCheckTasksDueTomorrow_WithNoTasks() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(taskRepo.findByDueDateAndDeletedFalse(tomorrow))
            .thenReturn(Collections.emptyList());

        // Act
        notificationService.manualCheckTasksDueTomorrow();

        // Assert
        verify(taskRepo).findByDueDateAndDeletedFalse(tomorrow);
        verify(emailService, never()).sendTaskDueNotification(any(), any(), any());
    }

    @Test
    void testCheckTasksDueTomorrow_WithTaskWithoutEmail() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        User userWithoutEmail = new User();
        userWithoutEmail.setUsername("testuser");
        // No email set
        
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDueDate(tomorrow);
        task.setUser(userWithoutEmail);
        task.setDeleted(false);
        
        when(taskRepo.findByDueDateAndDeletedFalse(tomorrow))
            .thenReturn(Arrays.asList(task));

        // Act
        notificationService.manualCheckTasksDueTomorrow();

        // Assert
        verify(taskRepo).findByDueDateAndDeletedFalse(tomorrow);
        verify(emailService, never()).sendTaskDueNotification(any(), any(), any());
    }
}