# 🚀 Production Database Configuration Guide

## Overview
This guide will help you configure your existing Render PostgreSQL database with your Spring Boot application.

## 📋 Step-by-Step Configuration

### Step 1: Get Your Database Connection URL

1. **Login to Render Dashboard**
2. **Navigate to your PostgreSQL service**
3. **Copy the connection details:**
   - Look for "External Database URL" or "Connection String"
   - It should look like: `postgresql://username:password@hostname:port/database`

### Step 2: Configure Environment Variables in Render

1. **Go to your Web Service** (not the database service)
2. **Click on "Environment" tab**
3. **Add the following environment variables:**

#### Required Variables:
```
DATABASE_URL = [Your PostgreSQL connection URL from Step 1]
JWT_SECRET = ---
SECURITY_PASSWORD_PEPPER = ---
SECRET_KEY = ---
SPRING_PROFILES_ACTIVE = prod
```

#### Optional Variables (already set in render.yaml):
```
PORT = 8080
DDL_AUTO = update
SQL_INIT_MODE = never
JWT_ENABLED = true
LOG_LEVEL = INFO
```

### Step 3: Deploy Your Application

1. **Commit your changes:**
   ```bash
   git add .
   git commit -m "Configure PostgreSQL for production"
   git push origin main
   ```

2. **Render will automatically deploy** when you push to your connected branch

### Step 4: Verify Deployment

1. **Check your Render service logs** for successful startup
2. **Look for these log messages:**
   - `Started ToDoApplication in X.XXX seconds`
   - `Database connection successful`
   - No database connection errors

### Step 5: Test Your API

1. **Open your Render service URL**
2. **Test basic endpoints:**
   - `GET /` - Health check
   - `POST /auth/login` - Login functionality
   - `GET /tasks` - Task endpoints (after login)

## 🔒 Security Notes

- **Keep your keys secure** - Never commit them to version control
- **Regenerate keys** if compromised
- **Use HTTPS only** in production (Render provides this automatically)
- **Database credentials** are managed by Render

## 🐛 Troubleshooting

### Database Connection Issues:
- Verify DATABASE_URL format: `postgresql://username:password@host:port/database`
- Check if database service is running in Render
- Ensure web service and database are in the same region

### JWT/Security Issues:
- Verify JWT_SECRET is exactly as generated (256-bit base64)
- Check SPRING_PROFILES_ACTIVE is set to "prod"
- Ensure all security keys are properly set

### Application Startup Issues:
- Check Render service logs for detailed error messages
- Verify Java version compatibility
- Check memory limits in Render

## 📞 Support

If you encounter issues:
1. Check Render service logs first
2. Verify all environment variables are set correctly
3. Test database connection independently
4. Review application logs for specific error messages

---

**✅ Your application is now configured for production with PostgreSQL!**
