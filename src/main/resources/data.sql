-- Initial data script for ToDoList application
-- This script will run automatically when the application starts

-- Insert sample users (passwords are encoded using BCrypt)
-- admin/admin123, john/password123, jane/password123
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@todolist.com', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('john', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'john@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('jane', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'jane@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('mike', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'mike@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('sarah', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'sarah@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample tasks for different users
INSERT INTO tasks (title, description, due_date, status, create_date, update_date, deleted, completion_date, category, priority, user_id) VALUES
-- John's tasks
('Complete Project Proposal', 'Finish the Q4 project proposal document', '2025-08-30', 'IN_PROGRESS', '2025-08-20', '2025-08-22', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'john')),
('Buy Groceries', 'Weekly grocery shopping for the family', '2025-08-25', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'john')),
('Team Meeting Preparation', 'Prepare slides for Monday team meeting', '2025-08-26', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'john')),
('Fix Website Bug', 'Resolve login page responsive design issue', '2025-08-28', 'COMPLETED', '2025-08-21', '2025-08-22', false, '2025-08-22', 'Work', 'MEDIUM', (SELECT id FROM users WHERE username = 'john')),

-- Jane's tasks
('Plan Birthday Party', 'Organize surprise birthday party for mom', '2025-09-05', 'IN_PROGRESS', '2025-08-20', '2025-08-23', false, null, 'Personal', 'HIGH', (SELECT id FROM users WHERE username = 'jane')),
('Code Review', 'Review pull requests from the development team', '2025-08-24', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Work', 'MEDIUM', (SELECT id FROM users WHERE username = 'jane')),
('Gym Membership Renewal', 'Renew annual gym membership', '2025-08-27', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Health', 'LOW', (SELECT id FROM users WHERE username = 'jane')),
('Database Migration', 'Migrate production database to new server', '2025-09-01', 'PENDING', '2025-08-21', '2025-08-21', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'jane')),

-- Mike's tasks
('Learn Spring Boot', 'Complete Spring Boot tutorial course', '2025-09-15', 'IN_PROGRESS', '2025-08-15', '2025-08-23', false, null, 'Learning', 'MEDIUM', (SELECT id FROM users WHERE username = 'mike')),
('Car Maintenance', 'Schedule car service appointment', '2025-08-29', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'mike')),
('Update Resume', 'Update resume with recent project experience', '2025-08-31', 'COMPLETED', '2025-08-20', '2025-08-21', false, '2025-08-21', 'Career', 'LOW', (SELECT id FROM users WHERE username = 'mike')),

-- Sarah's tasks
('Write Blog Post', 'Write technical blog post about microservices', '2025-09-10', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Writing', 'MEDIUM', (SELECT id FROM users WHERE username = 'sarah')),
('Doctor Appointment', 'Annual health checkup appointment', '2025-08-26', 'PENDING', '2025-08-20', '2025-08-20', false, null, 'Health', 'HIGH', (SELECT id FROM users WHERE username = 'sarah')),
('Clean House', 'Deep clean the house before guests arrive', '2025-08-24', 'IN_PROGRESS', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'sarah')),

-- Admin's tasks
('System Backup', 'Perform weekly system backup', '2025-08-25', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Admin', 'HIGH', (SELECT id FROM users WHERE username = 'admin')),
('User Account Audit', 'Review and audit user accounts for security', '2025-08-28', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Admin', 'MEDIUM', (SELECT id FROM users WHERE username = 'admin'));

-- Insert some collaboration relationships
-- John and Jane collaborate on work projects
INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Complete Project Proposal' LIMIT 1), (SELECT id FROM users WHERE username = 'jane'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Database Migration' LIMIT 1), (SELECT id FROM users WHERE username = 'john'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'System Backup' LIMIT 1), (SELECT id FROM users WHERE username = 'mike'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Code Review' LIMIT 1), (SELECT id FROM users WHERE username = 'mike'));
