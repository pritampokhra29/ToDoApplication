-- Production data initialization script
-- Run this ONCE when setting up production database

-- Create admin user for production
-- Password: admin123 + production pepper
-- NOTE: Replace 'YOUR_PRODUCTION_PEPPER_HERE' with your actual production pepper value
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
VALUES ('admin', 'admin123YOUR_PRODUCTION_PEPPER_HERE', 'admin@todolist.com', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Create a sample regular user for testing
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
VALUES ('user', 'password123YOUR_PRODUCTION_PEPPER_HERE', 'user@todolist.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Create sample task for admin
INSERT INTO tasks (title, description, due_date, status, create_date, update_date, deleted, completion_date, category, priority, user_id) 
VALUES ('Welcome Task', 'Welcome to ToDoList! This is your first task.', CURRENT_DATE + INTERVAL '7 days', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, null, 'System', 'LOW', 
        (SELECT id FROM users WHERE username = 'admin'))
ON CONFLICT DO NOTHING;
