# 🔐 JWT Authentication System

This document describes how to use the JWT-based authentication system with roles and permissions in the e-empuzitsi LMS application.

## 🚀 Features

- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Role-based Access Control**: Three default roles (STUDENT, TEACHER, ADMIN)
- **Permission-based Authorization**: Fine-grained permissions for specific actions
- **BCrypt Password Encryption**: Secure password hashing
- **Global Exception Handling**: Structured error responses
- **Automatic Role Assignment**: New users automatically get STUDENT role

## 🏗️ Architecture Overview

### Components

1. **Entities**: User, Role, Permission, UserRole, UserPermission
2. **Security**: JWT utilities, authentication filter, user details service
3. **Services**: AuthService (authentication), UserService (user management)
4. **Controllers**: AuthController (REST endpoints)
5. **Configuration**: Security config, data initializer

## 📝 API Endpoints

### Authentication Endpoints (Public)

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "roles": ["STUDENT"],
    "permissions": ["take_quiz", "view_lessons", "participate_discussion"],
    "createdAt": "2025-08-27T12:00:00",
    "updatedAt": "2025-08-27T12:00:00"
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:** Same as register response

### Protected Endpoints (Require Authentication)

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "roles": ["STUDENT"],
  "permissions": ["take_quiz", "view_lessons", "participate_discussion"],
  "createdAt": "2025-08-27T12:00:00",
  "updatedAt": "2025-08-27T12:00:00"
}
```

### Role Testing Endpoints

```http
GET /api/auth/student-test    # Requires STUDENT role
GET /api/auth/teacher-test    # Requires TEACHER role
GET /api/auth/admin-test      # Requires ADMIN role
GET /api/auth/upload-test     # Requires upload_lesson permission
GET /api/auth/quiz-test       # Requires create_quiz permission
```

## 👥 Default Roles & Permissions

### STUDENT Role
- `take_quiz`
- `view_lessons`
- `participate_discussion`

### TEACHER Role
- All STUDENT permissions +
- `upload_lesson`
- `create_quiz`
- `grade_quiz`
- `manage_students`
- `view_reports`
- `manage_subjects`

### ADMIN Role
- All permissions (full access)

### Complete Permission List
- `upload_lesson`
- `create_quiz`
- `grade_quiz`
- `manage_students`
- `view_reports`
- `manage_subjects`
- `manage_users`
- `manage_roles`
- `manage_permissions`
- `view_analytics`
- `take_quiz`
- `view_lessons`
- `participate_discussion`

## 🔧 Usage in Controllers

### Role-based Authorization
```java
@GetMapping("/teacher-only")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<String> teacherEndpoint() {
    return ResponseEntity.ok("Teacher content");
}
```

### Permission-based Authorization
```java
@PostMapping("/lessons")
@PreAuthorize("hasAuthority('upload_lesson')")
public ResponseEntity<String> uploadLesson() {
    return ResponseEntity.ok("Lesson uploaded");
}
```

### Multiple Conditions
```java
@GetMapping("/advanced")
@PreAuthorize("hasRole('TEACHER') and hasAuthority('view_reports')")
public ResponseEntity<String> advancedEndpoint() {
    return ResponseEntity.ok("Advanced content");
}
```

## 🔒 Security Configuration

### JWT Configuration
- **Secret**: Configured in `application.properties`
- **Expiration**: 24 hours (86400 seconds)
- **Token Format**: `Authorization: Bearer <token>`

### CORS Configuration
- **Allowed Origins**: All (configure for production)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS, HEAD
- **Allowed Headers**: All
- **Credentials**: Enabled

## 🚦 Testing the Authentication

### 1. Start the Application
```bash
./mvnw spring-boot:run
```

### 2. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 4. Use the Token
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <your_jwt_token>"
```

## 🛠️ Database Setup

The system will automatically create the required tables and initialize default roles and permissions on startup.

### Required Tables
- `users`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `user_permissions`

## ⚠️ Error Responses

### Validation Error (400)
```json
{
  "message": "Validation failed",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/auth/register",
  "timestamp": "2025-08-27T12:00:00",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid"
    }
  ]
}
```

### Authentication Error (401)
```json
{
  "message": "Authentication failed: Invalid email or password",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/auth/login",
  "timestamp": "2025-08-27T12:00:00"
}
```

### Access Denied (403)
```json
{
  "message": "Access denied: Access is denied",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/auth/teacher-test",
  "timestamp": "2025-08-27T12:00:00"
}
```

## 🔧 Configuration

### Application Properties
```properties
# JWT Configuration
app.jwt.secret=your-256-bit-secret-key
app.jwt.expiration=86400

# Security Logging
logging.level.com.mphoola.e_empuzitsi.security=DEBUG
```

## 📚 Next Steps

1. **Create Role Management API**: Add endpoints to manage roles and permissions
2. **Add Email Verification**: Implement email verification during registration
3. **Add Password Reset**: Implement password reset functionality
4. **Add Refresh Tokens**: Implement token refresh mechanism
5. **Add Rate Limiting**: Implement rate limiting for auth endpoints
6. **Add User Profile Management**: Allow users to update their profiles

## 🧪 Testing

The system includes comprehensive test endpoints for verifying role and permission-based access control. Use the provided test endpoints to verify your authentication setup is working correctly.
