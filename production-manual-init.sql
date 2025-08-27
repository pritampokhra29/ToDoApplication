-- ===============================================
-- PRODUCTION DATABASE MANUAL INITIALIZATION
-- ===============================================
-- Run this script ONCE in your production PostgreSQL database
-- to create the initial admin user and test data

-- Note: Replace 'YOUR_PRODUCTION_PEPPER_HERE' with your actual 
-- SECURITY_PASSWORD_PEPPER value from environment variables

-- ===============================================
-- Create Admin User
-- ===============================================
-- Username: admin
-- Password: admin123 (will be: admin123 + your production pepper)
-- IMPORTANT: The password stored here should be rawPassword + pepper
-- For example, if your pepper is "myProductionPepper123", then:
-- Password should be: admin123myProductionPepper123

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
VALUES (
    'admin', 
    'admin123YOUR_PRODUCTION_PEPPER_HERE',  -- Replace YOUR_PRODUCTION_PEPPER_HERE
    'admin@todolist.com', 
    'ADMIN', 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- ===============================================
-- Create Test User (Optional)
-- ===============================================
-- Username: user  
-- Password: password123 (will be: password123 + your production pepper)

INSERT INTO users (username, password, email, role, is_active, created_at, updated_at) 
VALUES (
    'user', 
    'password123YOUR_PRODUCTION_PEPPER_HERE',  -- Replace YOUR_PRODUCTION_PEPPER_HERE
    'user@todolist.com', 
    'USER', 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- ===============================================
-- Create Welcome Task for Admin (Optional)
-- ===============================================
INSERT INTO tasks (
    title, 
    description, 
    due_date, 
    status, 
    create_date, 
    update_date, 
    deleted, 
    completion_date, 
    category, 
    priority, 
    user_id
) 
SELECT 
    'Welcome to ToDoList!',
    'This is your first task. Feel free to edit or delete it.',
    CURRENT_DATE + INTERVAL '7 days',
    'PENDING',
    CURRENT_DATE,
    CURRENT_DATE,
    false,
    null,
    'Welcome',
    'LOW',
    u.id
FROM users u 
WHERE u.username = 'admin' 
AND NOT EXISTS (
    SELECT 1 FROM tasks 
    WHERE title = 'Welcome to ToDoList!' 
    AND user_id = u.id
);

-- ===============================================
-- VERIFICATION QUERIES
-- ===============================================
-- Run these to verify the data was inserted correctly:

-- Check users
-- SELECT id, username, email, role, is_active, created_at FROM users;

-- Check tasks  
-- SELECT id, title, description, status, category, priority, user_id FROM tasks;

-- ===============================================
-- INSTRUCTIONS:
-- ===============================================
-- 1. Replace 'YOUR_PRODUCTION_PEPPER_HERE' with your actual pepper value
-- 2. Run this script in your production PostgreSQL database
-- 3. Verify with the queries above
-- 4. Try logging in with: admin/admin123 and user/password123
