# Production Data Initialization Guide

## Problem Resolved ✅

The error you encountered was due to **missing initial data in the production database**:
```
User not found: admin
```

## Root Cause Analysis

1. **Development vs Production**: Data initialization was only configured for development (H2 database)
2. **Production Database**: PostgreSQL database was empty, no admin user existed
3. **Manual Setup Required**: Production databases should be initialized manually for security

## Solution: Manual Database Initialization

### Step 1: Prepare the SQL Script

Use the provided `production-manual-init.sql` script with your actual production pepper:

1. **Open** `production-manual-init.sql`
2. **Replace** `YOUR_PRODUCTION_PEPPER_HERE` with your actual `SECURITY_PASSWORD_PEPPER` value
3. **Save** the modified script

### Step 2: Execute in Production Database

**Option A: Using Render Dashboard**
1. Go to your Render PostgreSQL dashboard
2. Click "Connect" → "External Connection"  
3. Use a PostgreSQL client (pgAdmin, DBeaver, etc.)
4. Run the modified SQL script

**Option B: Using psql Command Line**
```bash
psql "postgresql://username:password@host/database" -f production-manual-init.sql
```

### Step 3: Verify Installation

Run these verification queries in your database:
```sql
-- Check users were created
SELECT id, username, email, role, is_active FROM users;

-- Check welcome task was created  
SELECT id, title, description, status, user_id FROM tasks;
```

## Default Production Credentials

After running the script, you'll have:

### Admin User:
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@todolist.com`
- **Role**: `ADMIN`

### Test User:
- **Username**: `user`
- **Password**: `password123`
- **Email**: `user@todolist.com`
- **Role**: `USER`

## Security Features

### Password Encoding:
- Uses **production pepper** from `SECURITY_PASSWORD_PEPPER`
- **Manual control** over initialization
- **No automatic data creation** in production

### Safe Operations:
- **ON CONFLICT DO NOTHING** prevents duplicate creation
- **Idempotent script** - can run multiple times safely
- **Manual verification** steps included

## Environment Variables Required

For production deployment, set these in Render:

```bash
# Database Configuration
DATABASE_JDBC_URL=jdbc:postgresql://your-host/your-db
DATABASE_USERNAME=your-username  
DATABASE_PASSWORD=your-password

# Security Configuration
JWT_SECRET=your-jwt-secret
SECURITY_PASSWORD_PEPPER=your-production-pepper
SECRET_KEY=your-secret-key

# Profile
SPRING_PROFILES_ACTIVE=prod
```

**Note**: No `INIT_PRODUCTION_DATA` variable needed anymore!

## Verification Steps

After deployment and database initialization:

1. **Check application logs** - should start without errors
2. **Try logging in** with admin/admin123 credentials
3. **Test API endpoints** with admin credentials
4. **Verify CORS** with your frontend at `https://todoapplication-frontend-mtl2.onrender.com`

## Important Security Notes

⚠️ **Change default passwords** after first login
⚠️ **Use strong production pepper** different from development  
⚠️ **Keep SQL script secure** - contains pepper information
⚠️ **Delete modified script** after use to prevent credential exposure

## Troubleshooting

### If login still fails:
1. **Check pepper value** in your environment variables
2. **Verify SQL script** was modified with correct pepper
3. **Check database** that users were actually created
4. **Review application logs** for authentication errors

This manual approach gives you **full control** over your production data initialization while maintaining security best practices! 🎉
