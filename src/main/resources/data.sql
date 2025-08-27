-- Initial data script for ToDoList application
-- This script will run automatically when the application starts

-- Insert sample users (passwords are encoded using custom encoder with pepper)
-- Development passwords: admin/admin123, john/password123, jane/password123, mike/password123, sarah/password123
-- Password format: rawPassword + pepper (devPepperForPasswordHashing123)
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('admin', 'admin123devPepperForPasswordHashing123', 'admin@todolist.com', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('john', 'password123devPepperForPasswordHashing123', 'john@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('jane', 'password123devPepperForPasswordHashing123', 'jane@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('mike', 'password123devPepperForPasswordHashing123', 'mike@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) VALUES 
('sarah', 'password123devPepperForPasswordHashing123', 'sarah@example.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample tasks for different users
INSERT INTO tasks (title, description, due_date, status, create_date, update_date, deleted, completion_date, category, priority, user_id) VALUES
-- John's tasks (6 tasks - expanded)
('Complete Project Proposal', 'Finish the Q4 project proposal document with budget analysis', '2025-08-30', 'IN_PROGRESS', '2025-08-20', '2025-08-22', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'john')),
('Buy Groceries', 'Weekly grocery shopping for the family - milk, bread, vegetables', '2025-08-25', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'john')),
('Team Meeting Preparation', 'Prepare slides for Monday team meeting about new features', '2025-08-26', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'john')),
('Fix Website Bug', 'Resolve login page responsive design issue on mobile devices', '2025-08-28', 'COMPLETED', '2025-08-21', '2025-08-22', false, '2025-08-22', 'Work', 'MEDIUM', (SELECT id FROM users WHERE username = 'john')),
('Call Insurance Company', 'Urgent call to discuss car insurance renewal and coverage', '2025-08-24', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'HIGH', (SELECT id FROM users WHERE username = 'john')),
('Review Code Documentation', 'Review and update API documentation for the new release', '2025-09-02', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Work', 'LOW', (SELECT id FROM users WHERE username = 'john')),

-- Jane's tasks (6 tasks - expanded)
('Plan Birthday Party', 'Organize surprise birthday party for mom - venue, catering, guests', '2025-09-05', 'IN_PROGRESS', '2025-08-20', '2025-08-23', false, null, 'Personal', 'HIGH', (SELECT id FROM users WHERE username = 'jane')),
('Code Review', 'Review pull requests from the development team for sprint 15', '2025-08-24', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Work', 'MEDIUM', (SELECT id FROM users WHERE username = 'jane')),
('Gym Membership Renewal', 'Renew annual gym membership before it expires', '2025-08-27', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Health', 'LOW', (SELECT id FROM users WHERE username = 'jane')),
('Database Migration', 'Migrate production database to new server with zero downtime', '2025-09-01', 'PENDING', '2025-08-21', '2025-08-21', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'jane')),
('Dentist Appointment', 'Routine dental checkup and cleaning', '2025-08-29', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Health', 'MEDIUM', (SELECT id FROM users WHERE username = 'jane')),
('Client Presentation', 'Present quarterly results to key client stakeholders', '2025-08-31', 'IN_PROGRESS', '2025-08-20', '2025-08-23', false, null, 'Work', 'HIGH', (SELECT id FROM users WHERE username = 'jane')),

-- Mike's tasks (5 tasks - expanded)
('Learn Spring Boot', 'Complete Spring Boot tutorial course and practice examples', '2025-09-15', 'IN_PROGRESS', '2025-08-15', '2025-08-23', false, null, 'Learning', 'MEDIUM', (SELECT id FROM users WHERE username = 'mike')),
('Car Maintenance', 'Schedule car service appointment for oil change and inspection', '2025-08-29', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'mike')),
('Update Resume', 'Update resume with recent project experience and skills', '2025-08-31', 'COMPLETED', '2025-08-20', '2025-08-21', false, '2025-08-21', 'Career', 'LOW', (SELECT id FROM users WHERE username = 'mike')),
('Prepare Technical Interview', 'Study algorithms and system design for upcoming interviews', '2025-09-10', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Career', 'HIGH', (SELECT id FROM users WHERE username = 'mike')),
('Buy Birthday Gift', 'Find and buy birthday gift for sister', '2025-08-26', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'mike')),

-- Sarah's tasks (5 tasks - expanded)
('Write Blog Post', 'Write technical blog post about microservices architecture', '2025-09-10', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Writing', 'MEDIUM', (SELECT id FROM users WHERE username = 'sarah')),
('Doctor Appointment', 'Annual health checkup appointment with family physician', '2025-08-26', 'PENDING', '2025-08-20', '2025-08-20', false, null, 'Health', 'HIGH', (SELECT id FROM users WHERE username = 'sarah')),
('Clean House', 'Deep clean the house before guests arrive for weekend', '2025-08-24', 'IN_PROGRESS', '2025-08-23', '2025-08-23', false, null, 'Personal', 'MEDIUM', (SELECT id FROM users WHERE username = 'sarah')),
('Submit Tax Documents', 'Gather and submit quarterly tax documents to accountant', '2025-08-30', 'PENDING', '2025-08-21', '2025-08-21', false, null, 'Finance', 'HIGH', (SELECT id FROM users WHERE username = 'sarah')),
('Plan Vacation', 'Research and plan summer vacation destinations and bookings', '2025-09-15', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Personal', 'LOW', (SELECT id FROM users WHERE username = 'sarah')),

-- Admin's tasks (4 tasks - expanded)
('System Backup', 'Perform weekly system backup and verify data integrity', '2025-08-25', 'PENDING', '2025-08-23', '2025-08-23', false, null, 'Admin', 'HIGH', (SELECT id FROM users WHERE username = 'admin')),
('User Account Audit', 'Review and audit user accounts for security compliance', '2025-08-28', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Admin', 'MEDIUM', (SELECT id FROM users WHERE username = 'admin')),
('Server Maintenance', 'Scheduled maintenance on production servers', '2025-08-27', 'PENDING', '2025-08-22', '2025-08-22', false, null, 'Admin', 'HIGH', (SELECT id FROM users WHERE username = 'admin')),
('Security Patch Update', 'Apply latest security patches to all systems', '2025-08-24', 'COMPLETED', '2025-08-21', '2025-08-22', false, '2025-08-22', 'Admin', 'HIGH', (SELECT id FROM users WHERE username = 'admin'));

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

-- Additional collaborations
INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Client Presentation' LIMIT 1), (SELECT id FROM users WHERE username = 'john'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Server Maintenance' LIMIT 1), (SELECT id FROM users WHERE username = 'jane'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Team Meeting Preparation' LIMIT 1), (SELECT id FROM users WHERE username = 'sarah'));

INSERT INTO task_collaborators (task_id, user_id) VALUES
((SELECT id FROM tasks WHERE title = 'Write Blog Post' LIMIT 1), (SELECT id FROM users WHERE username = 'mike'));
