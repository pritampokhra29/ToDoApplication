-- Production data initialization script
-- This script runs when INIT_PRODUCTION_DATA=always is set

-- Create admin user for production
-- The password will use the production pepper from environment variables
-- This is safe because the pepper is applied by the application layer, not in SQL

-- Check if admin user exists first, if not create it
-- Note: We use a placeholder password that will be processed by the application
-- The actual password encoding happens in the application layer with the production pepper

-- Insert admin user if not exists
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
SELECT 'admin', 'admin123', 'admin@todolist.com', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Insert a test user if not exists  
INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
SELECT 'user', 'password123', 'user@todolist.com', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');

-- Insert welcome task for admin
INSERT INTO tasks (title, description, due_date, status, create_date, update_date, deleted, completion_date, category, priority, user_id) 
SELECT 'Welcome to ToDoList!', 'This is your first task. Feel free to edit or delete it.', CURRENT_DATE + INTERVAL '7 days', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, null, 'Welcome', 'LOW', u.id
FROM users u 
WHERE u.username = 'admin' 
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = 'Welcome to ToDoList!' AND user_id = u.id);
