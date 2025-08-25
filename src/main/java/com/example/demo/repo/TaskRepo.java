package com.example.demo.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // **NEW: Pagination and Sorting methods**
    Page<Task> findByUserUsernameAndDeletedFalse(String username, Pageable pageable);
    
    // **NEW: Due date filtering**
    List<Task> findByUserUsernameAndDueDateAndDeletedFalse(String username, LocalDate dueDate);
    List<Task> findByUserUsernameAndDueDateBetweenAndDeletedFalse(String username, LocalDate startDate, LocalDate endDate);
    List<Task> findByUserUsernameAndDueDateBeforeAndDeletedFalse(String username, LocalDate date);
    List<Task> findByUserUsernameAndDueDateAfterAndDeletedFalse(String username, LocalDate date);
    
    // **NEW: Global due date filtering for notifications**
    List<Task> findByDueDateAndDeletedFalse(LocalDate dueDate);
    
    // **NEW: Search by keyword in title or description**
    @Query("SELECT t FROM Task t WHERE t.user.username = :username AND t.deleted = false AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> findByUserUsernameAndKeywordInTitleOrDescription(@Param("username") String username, @Param("keyword") String keyword);
    
    // **NEW: Search with pagination**
    @Query("SELECT t FROM Task t WHERE t.user.username = :username AND t.deleted = false AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> findByUserUsernameAndKeywordInTitleOrDescription(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);
    
    // **NEW: Advanced filtering with pagination**
    @Query("SELECT t FROM Task t WHERE t.user.username = :username AND t.deleted = false AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:dueDate IS NULL OR t.dueDate = :dueDate)")
    Page<Task> findTasksWithFilters(@Param("username") String username, 
                                   @Param("status") com.example.demo.constants.Status status,
                                   @Param("category") String category,
                                   @Param("dueDate") LocalDate dueDate,
                                   Pageable pageable);
    
    // **NEW: Check if a user is already a collaborator on a task**
    @Query("SELECT COUNT(t) > 0 FROM Task t JOIN t.collaborators c WHERE t.id = :taskId AND c.id = :userId")
    boolean existsCollaboratorByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
}