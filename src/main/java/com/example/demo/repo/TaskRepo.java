package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {
    
    // Basic methods (existing and working)
    List<Task> findByUserUsername(String username);
    Optional<Task> findByIdAndUserUsername(Long id, String username);
    
    // Enhanced methods with proper soft delete support
    List<Task> findByUserUsernameAndDeletedFalse(String username);
    Optional<Task> findByIdAndUserUsernameAndDeletedFalse(Long id, String username);
    
    // Methods for filtering
    List<Task> findByUserUsernameAndStatusAndDeletedFalse(String username, com.example.demo.constants.Status status);
    List<Task> findByUserUsernameAndCategoryAndDeletedFalse(String username, String category);
    
    // Additional filtering methods for collaborator support
    List<Task> findByStatusAndDeletedFalse(com.example.demo.constants.Status status);
    List<Task> findByCategoryAndDeletedFalse(String category);
    
    // Methods to get all tasks (we'll filter collaborators in service layer)
    List<Task> findAllByDeletedFalse();
    Optional<Task> findByIdAndDeletedFalse(Long id);
    
    // Get all deleted tasks for recovery purposes
    List<Task> findByUserUsernameAndDeletedTrue(String username);
}