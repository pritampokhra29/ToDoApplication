# Advanced Features API Documentation

This document describes the newly implemented advanced features for search, filtering, pagination, and sorting.

## 🔍 Search & Filters

### 1. Search by Keyword

**Endpoint:** `POST /tasks/search`

**Description:** Search tasks by keyword in title or description

**Request Body:**
```json
{
    "keyword": "meeting"
}
```

**Response:** List of tasks matching the keyword

**Example:**
```bash
curl -X POST http://localhost:8080/tasks/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic am9objpwYXNzd29yZDEyMw==" \
  -d '{"keyword": "project"}'
```

### 2. Filter by Due Date

**Endpoint:** `POST /tasks/filter/duedate`

**Description:** Get tasks with a specific due date

**Request Body:**
```json
{
    "dueDate": "2025-08-30"
}
```

### 3. Filter by Due Date Range

**Endpoint:** `POST /tasks/filter/duedate/range`

**Description:** Get tasks within a date range

**Request Body:**
```json
{
    "startDate": "2025-08-25",
    "endDate": "2025-08-30"
}
```

### 4. Filter Tasks Due Before Date

**Endpoint:** `POST /tasks/filter/duedate/before`

**Description:** Get tasks due before a specific date

**Request Body:**
```json
{
    "date": "2025-08-30"
}
```

### 5. Filter Tasks Due After Date

**Endpoint:** `POST /tasks/filter/duedate/after`

**Description:** Get tasks due after a specific date

**Request Body:**
```json
{
    "date": "2025-08-25"
}
```

## 📄 Pagination & Sorting

### 1. Get Tasks with Pagination

**Endpoint:** `GET /tasks/paginated`

**Description:** Get paginated list of tasks with sorting

**Query Parameters:**
- `page` (default: 0) - Page number (0-based)
- `size` (default: 10) - Number of items per page
- `sortBy` (default: "id") - Field to sort by
- `sortDir` (default: "asc") - Sort direction ("asc" or "desc")

**Available Sort Fields:**
- `id` - Task ID
- `title` - Task title
- `dueDate` - Due date
- `priority` - Priority level
- `status` - Task status
- `createDate` - Creation date
- `updateDate` - Last update date

**Example:**
```bash
# Get page 1 (second page) with 5 items, sorted by due date descending
GET /tasks/paginated?page=1&size=5&sortBy=dueDate&sortDir=desc
```

**Response Format:**
```json
{
    "content": [...tasks...],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {...}
    },
    "totalPages": 3,
    "totalElements": 25,
    "last": false,
    "first": true,
    "numberOfElements": 10
}
```

### 2. Search with Pagination

**Endpoint:** `POST /tasks/search/paginated`

**Description:** Search tasks with pagination and sorting

**Query Parameters:** Same as above pagination parameters

**Request Body:**
```json
{
    "keyword": "important"
}
```

**Example:**
```bash
POST /tasks/search/paginated?page=0&size=5&sortBy=priority&sortDir=desc
```

## 🎯 Advanced Filtering

### Combined Filters with Pagination

**Endpoint:** `GET /tasks/advanced`

**Description:** Advanced filtering with multiple criteria and pagination

**Query Parameters:**
- `status` (optional) - Filter by status (PENDING, IN_PROGRESS, COMPLETED)
- `category` (optional) - Filter by category
- `dueDate` (optional) - Filter by specific due date (YYYY-MM-DD)
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `sortBy` (default: "dueDate") - Sort field
- `sortDir` (default: "asc") - Sort direction

**Examples:**

```bash
# Get pending work tasks, sorted by priority
GET /tasks/advanced?status=PENDING&category=Work&sortBy=priority&sortDir=desc

# Get tasks due on specific date with pagination
GET /tasks/advanced?dueDate=2025-08-30&page=0&size=5

# Get all tasks in Personal category, sorted by due date
GET /tasks/advanced?category=Personal&sortBy=dueDate&sortDir=asc
```

## 📊 Response Examples

### Paginated Response
```json
{
    "content": [
        {
            "id": 1,
            "title": "Complete Project Proposal",
            "description": "Finish the Q4 project proposal document",
            "dueDate": "2025-08-30",
            "status": "IN_PROGRESS",
            "category": "Work",
            "priority": "HIGH",
            "createDate": "2025-08-20",
            "updateDate": "2025-08-22",
            "deleted": false,
            "collaborators": [...]
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "sorted": true,
            "ascending": false,
            "by": "dueDate"
        }
    },
    "totalPages": 2,
    "totalElements": 16,
    "last": false,
    "first": true,
    "numberOfElements": 10
}
```

### Search Response
```json
[
    {
        "id": 5,
        "title": "Team Meeting Preparation",
        "description": "Prepare slides for Monday team meeting",
        "dueDate": "2025-08-26",
        "status": "PENDING",
        "category": "Work",
        "priority": "HIGH"
    }
]
```

## 🚀 Usage Scenarios

### 1. Dashboard View
```bash
# Get recent tasks, sorted by due date
GET /tasks/advanced?sortBy=dueDate&sortDir=asc&size=5
```

### 2. Search for Urgent Tasks
```bash
# Search for "urgent" tasks
POST /tasks/search
{"keyword": "urgent"}
```

### 3. Weekly Planning
```bash
# Get tasks due this week
POST /tasks/filter/duedate/range
{
    "startDate": "2025-08-25",
    "endDate": "2025-08-31"
}
```

### 4. Overdue Tasks
```bash
# Get tasks due before today
POST /tasks/filter/duedate/before
{"date": "2025-08-23"}
```

### 5. Work Tasks by Priority
```bash
# Get work tasks sorted by priority (high to low)
GET /tasks/advanced?category=Work&sortBy=priority&sortDir=desc
```

## 🔐 Authentication

All endpoints require authentication. Use HTTP Basic Auth with username:password.

**Examples:**
- john:password123
- jane:password123
- admin:admin123

## 📈 Performance Notes

- **Pagination** - Default page size is 10, maximum recommended is 100
- **Sorting** - All major fields are indexed for performance
- **Search** - Uses case-insensitive LIKE queries
- **Collaborators** - Advanced features work with collaborative tasks
- **Caching** - Consider implementing for frequently accessed data

## 🧪 Testing with Dummy Data

The application includes sample data that demonstrates all features:

- **16 tasks** across 5 users
- **4 collaboration relationships**
- **Various categories**: Work, Personal, Health, Learning, etc.
- **Different priorities**: HIGH, MEDIUM, LOW
- **Mixed statuses**: PENDING, IN_PROGRESS, COMPLETED
- **Date range**: Tasks from 2025-08-23 to 2025-09-15
