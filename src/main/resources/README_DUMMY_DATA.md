# Dummy Data Initialization

This document describes the initial dummy data that gets loaded when the ToDoList application starts.

## Database Initialization

The application uses two SQL scripts for initialization:

### 1. `schema.sql` - Database Schema
- Creates tables: `users`, `tasks`, `task_collaborators`
- Sets up foreign key relationships
- Creates performance indexes

### 2. `data.sql` - Sample Data
- Populates the database with sample users and tasks
- Sets up collaboration relationships

## Sample Users

The following users are created automatically:

| Username | Password    | Email                | Role  |
|----------|-------------|---------------------|-------|
| admin    | admin123    | admin@todolist.com  | Admin |
| john     | password123 | john@example.com    | User  |
| jane     | password123 | jane@example.com    | User  |
| mike     | password123 | mike@example.com    | User  |
| sarah    | password123 | sarah@example.com   | User  |

**Note:** All passwords are BCrypt encoded in the database.

## Sample Tasks

### John's Tasks (4 tasks)
1. **Complete Project Proposal** (Work, HIGH) - IN_PROGRESS
   - Collaborator: jane
2. **Buy Groceries** (Personal, MEDIUM) - PENDING
3. **Team Meeting Preparation** (Work, HIGH) - PENDING
4. **Fix Website Bug** (Work, MEDIUM) - COMPLETED

### Jane's Tasks (4 tasks)
1. **Plan Birthday Party** (Personal, HIGH) - IN_PROGRESS
2. **Code Review** (Work, MEDIUM) - PENDING
   - Collaborator: mike
3. **Gym Membership Renewal** (Health, LOW) - PENDING
4. **Database Migration** (Work, HIGH) - PENDING
   - Collaborator: john

### Mike's Tasks (3 tasks)
1. **Learn Spring Boot** (Learning, MEDIUM) - IN_PROGRESS
2. **Car Maintenance** (Personal, MEDIUM) - PENDING
3. **Update Resume** (Career, LOW) - COMPLETED

### Sarah's Tasks (3 tasks)
1. **Write Blog Post** (Writing, MEDIUM) - PENDING
2. **Doctor Appointment** (Health, HIGH) - PENDING
3. **Clean House** (Personal, MEDIUM) - IN_PROGRESS

### Admin's Tasks (2 tasks)
1. **System Backup** (Admin, HIGH) - PENDING
   - Collaborator: mike
2. **User Account Audit** (Admin, MEDIUM) - PENDING

## Task Categories
- Work
- Personal
- Health
- Learning
- Writing
- Career
- Admin

## Priority Levels
- HIGH
- MEDIUM
- LOW

## Status Values
- PENDING (instead of TODO)
- IN_PROGRESS
- COMPLETED

## Collaboration Examples
The dummy data includes several collaboration relationships:
- John and Jane collaborate on work projects
- Mike helps with admin tasks and code reviews
- Tasks show how multiple users can work together

## Testing Scenarios

You can use this dummy data to test:

1. **User Authentication** - Login with any of the sample users
2. **Task Management** - View, create, update, delete tasks
3. **Collaboration** - See how shared tasks work
4. **Filtering** - Filter by status, category, priority
5. **User Isolation** - Each user sees only their own and collaborative tasks

## Database Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Resetting Data

Since this is an in-memory H2 database, all data resets when you restart the application. The scripts will re-run and recreate all the dummy data.
