-- =============================================================================
-- PostgreSQL Database Setup Script for ToDo Application
-- =============================================================================

-- Create database (run this as superuser)
-- CREATE DATABASE todoapp;

-- Create user and grant privileges
-- CREATE USER todouser WITH PASSWORD 'your_secure_password_here';
-- GRANT ALL PRIVILEGES ON DATABASE todoapp TO todouser;

-- Connect to todoapp database and run the following:

-- =============================================================================
-- Tables will be auto-created by Hibernate when DDL_AUTO=update
-- But here's the manual schema for reference:
-- =============================================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP
);

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    create_date DATE DEFAULT CURRENT_DATE,
    update_date DATE DEFAULT CURRENT_DATE,
    completion_date DATE,
    deleted BOOLEAN NOT NULL DEFAULT false,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

-- Task collaborators junction table
CREATE TABLE IF NOT EXISTS task_collaborators (
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_task_collaborators_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_collaborators_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_category ON tasks(category);
CREATE INDEX IF NOT EXISTS idx_tasks_deleted ON tasks(deleted);

CREATE INDEX IF NOT EXISTS idx_task_collaborators_task_id ON task_collaborators(task_id);
CREATE INDEX IF NOT EXISTS idx_task_collaborators_user_id ON task_collaborators(user_id);

-- =============================================================================
-- Sample data (optional - remove in production)
-- =============================================================================

-- Insert sample users (passwords should be properly hashed in real application)
-- INSERT INTO users (username, email, password, role, is_active) VALUES
-- ('admin', 'admin@todoapp.com', '$2a$10$hashed_password_here', 'ADMIN', true),
-- ('john', 'john@todoapp.com', '$2a$10$hashed_password_here', 'USER', true),
-- ('jane', 'jane@todoapp.com', '$2a$10$hashed_password_here', 'USER', true);

-- Grant permissions to todouser
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO todouser;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO todouser;
