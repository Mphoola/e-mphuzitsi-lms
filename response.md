# API Endpoints Requirements Document

This document outlines the request and response structures needed to convert the existing web endpoints to API endpoints for the Transport and Fleet Management System.

## Authentication & Authorization

All API endpoints require:
- **Authentication**: Bearer token in Authorization header
- **Permissions**: Each endpoint requires specific permissions as detailed below

## Common Response Structure

All API responses should follow this structure:

```json
{
  "success": true|false,
  "message": "Human readable message",
  "data": {}, // Response data
  "errors": {}, // Validation errors (if any)
  "meta": {} // Pagination/additional metadata (if applicable)
}
```

## 1. Activity Logs Endpoint

### GET /api/activity-logs

**Permission Required**: `list audit logs`

**Query Parameters**:
```
search: string (optional) - Search term for description, event, subject_type, causer name
page: integer (optional, min: 1) - Page number for pagination
per_page: integer (optional, min: 1, max: 500) - Items per page
sort_by: string (optional) - Column to sort by
sort_direction: string (optional) - asc|desc
```

**Response Structure**:
```json
{
  "success": true,
  "message": "Activity logs retrieved successfully",
  "data": {
    "current_page": 1,
    "data": [
      {
        "id": 1,
        "log_name": "default",
        "description": "User created",
        "subject_type": "App\\Models\\User",
        "subject_id": 1,
        "causer_type": "App\\Models\\User",
        "causer_id": 2,
        "causer_name": "John Doe",
        "properties": {},
        "event": "created",
        "created_at": "2025-01-01T10:00:00.000000Z",
        "updated_at": "2025-01-01T10:00:00.000000Z"
      }
    ],
    "first_page_url": "http://localhost:8000/api/activity-logs?page=1",
    "from": 1,
    "last_page": 5,
    "last_page_url": "http://localhost:8000/api/activity-logs?page=5",
    "links": [],
    "next_page_url": "http://localhost:8000/api/activity-logs?page=2",
    "path": "http://localhost:8000/api/activity-logs",
    "per_page": 10,
    "prev_page_url": null,
    "to": 10,
    "total": 50
  }
}
```

## 2. Users Management Endpoints

### GET /api/users

**Permission Required**: `list users`

**Query Parameters**: Same as Activity Logs + additional filters:
```
status: string (optional) - Filter by user status
```

**Response Structure**:
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "current_page": 1,
    "data": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "status": "Active",
        "created_at": "2025-01-01T10:00:00.000000Z",
        "updated_at": "2025-01-01T10:00:00.000000Z",
        "roles": [
          {
            "id": 1,
            "name": "admin"
          }
        ]
      }
    ],
    "pagination_data": "...",
    "filters": {
      "search": "",
      "status": "",
      "page": 1,
      "per_page": 10
    },
    "roles": [
      {
        "id": 1,
        "name": "admin"
      },
      {
        "id": 2,
        "name": "user"
      }
    ]
  }
}
```

### POST /api/users

**Permission Required**: `add user`

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "account_type": "Staff", // Applicant|Staff
  "role": "admin" // Must exist in roles table
}
```

**Validation Rules**:
- `name`: required|string|max:255
- `email`: required|email|max:255|unique:users,email
- `account_type`: required|in:Applicant,Staff
- `role`: required|exists:roles,name

**Response Structure**:
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "created_at": "2025-01-01T10:00:00.000000Z",
    "updated_at": "2025-01-01T10:00:00.000000Z",
    "roles": [
      {
        "id": 1,
        "name": "admin"
      }
    ]
  }
}
```

### GET /api/users/{id}

**Permission Required**: `see user details`

**Response Structure**:
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "status": "Active",
      "account_type": "Staff",
      "department_id": 1,
      "email_verified_at": "2025-01-01T10:00:00.000000Z",
      "created_at": "2025-01-01T10:00:00.000000Z",
      "updated_at": "2025-01-01T10:00:00.000000Z",
      "roles": [
        {
          "id": 1,
          "name": "admin"
        }
      ]
    },
    "roles": [
      {
        "id": 1,
        "name": "admin"
      }
    ],
    "all_permissions": [
      {
        "id": 1,
        "name": "view users"
      }
    ],
    "user_permissions_via_role": [
      {
        "id": 1,
        "name": "view users"
      }
    ],
    "user_direct_permissions": [],
    "logs": [
      {
        "id": 1,
        "description": "User created",
        "created_at": "2025-01-01T10:00:00.000000Z"
      }
    ]
  }
}
```

### PUT /api/users/{id}

**Permission Required**: `update user`

**Request Body**:
```json
{
  "name": "John Doe Updated",
  "account_type": "Staff",
  "role": "user"
}
```

**Validation Rules**: Same as POST except email is not required/updateable via this endpoint

**Response Structure**:
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "name": "John Doe Updated",
    "account_type": "Staff",
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

### DELETE /api/users/{id}

**Permission Required**: `delete user` (inferred)

**Response Structure**:
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

### PUT /api/users/{id}/activate

**Permission Required**: `suspend user`

**Response Structure**:
```json
{
  "success": true,
  "message": "User activated successfully",
  "data": {
    "id": 1,
    "status": "Active",
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

### PUT /api/users/{id}/ban

**Permission Required**: `suspend user`

**Response Structure**:
```json
{
  "success": true,
  "message": "User banned successfully",
  "data": {
    "id": 1,
    "status": "Banned",
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

## 3. User Roles and Permissions Endpoints

### PUT /api/users/{id}/role

**Permission Required**: `update user role`

**Request Body**:
```json
{
  "role": "admin" // Must exist in roles table
}
```

**Validation Rules**:
- `role`: required|exists:roles,name

**Response Structure**:
```json
{
  "success": true,
  "message": "Role changed successfully",
  "data": {
    "user_id": 1,
    "old_role": ["user"],
    "new_role": "admin",
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

### PUT /api/users/{id}/permissions

**Permission Required**: `add user permissions`

**Request Body**:
```json
{
  "permissions": ["view users", "edit users"] // Array of permission names
}
```

**Validation Rules**:
- `permissions`: required|array|min:1
- `permissions.*`: exists:permissions,name

**Response Structure**:
```json
{
  "success": true,
  "message": "Permissions changed successfully",
  "data": {
    "user_id": 1,
    "old_permissions": ["view users"],
    "new_permissions": ["view users", "edit users"],
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

## 4. Profile Management Endpoints

### GET /api/profile

**Permission Required**: None (authenticated user's own profile)

**Response Structure**:
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "email_verified_at": "2025-01-01T10:00:00.000000Z",
      "created_at": "2025-01-01T10:00:00.000000Z",
      "updated_at": "2025-01-01T10:00:00.000000Z"
    },
    "mustVerifyEmail": false,
    "status": null
  }
}
```

### PATCH /api/profile

**Permission Required**: None (authenticated user's own profile)

**Request Body**:
```json
{
  "name": "John Doe Updated",
  "email": "john.updated@example.com"
}
```

**Validation Rules**:
- `name`: required|string|max:255
- `email`: required|string|lowercase|email|max:255|unique:users,email,{user_id}

**Response Structure**:
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "name": "John Doe Updated",
    "email": "john.updated@example.com",
    "email_verified_at": null, // Reset if email changed
    "updated_at": "2025-01-01T11:00:00.000000Z"
  }
}
```

## Error Responses

### 400 Bad Request (Validation Errors)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": [
      "The email field is required."
    ],
    "name": [
      "The name field is required."
    ]
  }
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthenticated",
  "data": null,
  "errors": {}
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Insufficient permissions",
  "data": null,
  "errors": {}
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null,
  "errors": {}
}
```

### 500 Server Error
```json
{
  "success": false,
  "message": "Internal server error",
  "data": null,
  "errors": {}
}
```

## Additional Notes

1. **Activity Logging**: All user modification endpoints automatically log activities using the Spatie Activity Log package.

2. **Cache Management**: User permission changes automatically clear cached permissions.

3. **Notifications**: User creation and status changes trigger email notifications.

4. **Password Generation**: User creation generates a random 16-character password sent via email.

5. **Email Verification**: Email changes reset the email_verified_at field to null.

6. **Role Restrictions**: Some roles (Admin, Registrar, Transport Officer, HoD) have special handling in the system.

7. **Soft Deletes**: Check if User model uses soft deletes before implementing hard deletion.

8. **Rate Limiting**: Consider implementing rate limiting for API endpoints.

9. **API Versioning**: Consider versioning the API (e.g., /api/v1/users).

10. **Response Caching**: The HasDataTable trait supports caching for better performance.
