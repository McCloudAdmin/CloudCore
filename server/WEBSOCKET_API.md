# WebSocket API Documentation

## Overview
This document describes all available WebSocket API endpoints for the MCCloudAdmin server. All messages are sent and received in JSON format.

## Message Format
All messages are sent and received in JSON format with the following structure:
```json
{
    "type": "database",
    "content": null,
    "operation": "operation_name",
    "data": {} // Optional data for the operation
}
```

## Database Operations

### List Tables
Retrieves a list of all tables in the database.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listTables"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listTables",
    "data": ["mccloudadmin_users", "mccloudadmin_roles"],
    "success": true,
    "message": "Tables retrieved successfully"
}
```

### Get Table Structure
Retrieves the structure of a specific table.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getTableStructure",
    "data": "mccloudadmin_users"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getTableStructure",
    "data": [
        {
            "Field": "id",
            "Type": "int(11)",
            "Null": "NO",
            "Key": "PRI",
            "Default": null,
            "Extra": "auto_increment"
        },
        {
            "Field": "username",
            "Type": "text",
            "Null": "NO",
            "Key": "",
            "Default": null,
            "Extra": ""
        }
    ],
    "success": true,
    "message": "Table structure retrieved successfully"
}
```

## Settings Operations

### Get Setting
Retrieves a specific setting by name.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getSetting",
    "data": "setting_name"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getSetting",
    "data": "setting_value",
    "success": true,
    "message": "Setting retrieved successfully"
}
```

### Set Setting
Sets or updates a setting value.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "setSetting",
    "data": {
        "name": "setting_name",
        "value": "setting_value"
    }
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "setSetting",
    "data": "setting_value",
    "success": true,
    "message": "Setting updated successfully"
}
```

### Get All Settings
Retrieves all settings.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getAllSettings"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getAllSettings",
    "data": {
        "setting1": "value1",
        "setting2": "value2"
    },
    "success": true,
    "message": "Settings retrieved successfully"
}
```

### Delete Setting
Deletes a specific setting.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "deleteSetting",
    "data": "setting_name"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "deleteSetting",
    "success": true,
    "message": "Setting deleted successfully"
}
```

## User Operations

### List Users
Retrieves a list of all users.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listUsers"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listUsers",
    "data": [
        {
            "id": 1,
            "username": "user1",
            "first_name": "John",
            "last_name": "Doe",
            "email": "john@example.com",
            "avatar": "https://www.gravatar.com/avatar",
            "credits": 0,
            "background": "https://cdn.mythical.systems/background.gif",
            "uuid": "550e8400-e29b-41d4-a716-446655440000",
            "token": "user_token",
            "role": 1,
            "first_ip": "127.0.0.1",
            "last_ip": "127.0.0.1",
            "banned": "NO",
            "verified": "false",
            "support_pin": "1234",
            "2fa_enabled": "false",
            "2fa_key": null,
            "2fa_blocked": "false",
            "discord_id": null,
            "github_id": null,
            "github_username": null,
            "github_email": null,
            "github_linked": "false",
            "discord_username": null,
            "discord_global_name": null,
            "discord_email": null,
            "discord_linked": "false",
            "deleted": "false",
            "locked": "false",
            "last_seen": "2024-01-01T00:00:00Z",
            "first_seen": "2024-01-01T00:00:00Z"
        }
    ],
    "success": true,
    "message": "Users retrieved successfully"
}
```

### Get User by ID
Retrieves a user by their ID.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserById",
    "data": 1
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserById",
    "data": {
        "id": 1,
        "username": "user1",
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com",
        "is_active": true,
        "is_staff": false,
        "is_superuser": false,
        "date_joined": "2024-01-01T00:00:00Z",
        "last_login": "2024-01-02T00:00:00Z"
    },
    "success": true,
    "message": "User retrieved successfully"
}
```

### Get User by Email
Retrieves a user by their email address.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByEmail",
    "data": "john@example.com"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByEmail",
    "data": {
        "id": 1,
        "username": "user1",
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com",
        "is_active": true,
        "is_staff": false,
        "is_superuser": false,
        "date_joined": "2024-01-01T00:00:00Z",
        "last_login": "2024-01-02T00:00:00Z"
    },
    "success": true,
    "message": "User retrieved successfully"
}
```

### Get User by Username
Retrieves a user by their username.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByUsername",
    "data": "user1"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByUsername",
    "data": {
        "id": 1,
        "username": "user1",
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com",
        "is_active": true,
        "is_staff": false,
        "is_superuser": false,
        "date_joined": "2024-01-01T00:00:00Z",
        "last_login": "2024-01-02T00:00:00Z"
    },
    "success": true,
    "message": "User retrieved successfully"
}
```

### Get User by UUID
Retrieves a user by their UUID.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByUuid",
    "data": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getUserByUuid",
    "data": {
        "id": 1,
        "username": "user1",
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com",
        "avatar": "https://www.gravatar.com/avatar",
        "credits": 0,
        "background": "https://cdn.mythical.systems/background.gif",
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "token": "user_token",
        "role": 1,
        "first_ip": "127.0.0.1",
        "last_ip": "127.0.0.1",
        "banned": "NO",
        "verified": "false",
        "support_pin": "1234",
        "2fa_enabled": "false",
        "2fa_key": null,
        "2fa_blocked": "false",
        "discord_id": null,
        "github_id": null,
        "github_username": null,
        "github_email": null,
        "github_linked": "false",
        "discord_username": null,
        "discord_global_name": null,
        "discord_email": null,
        "discord_linked": "false",
        "deleted": "false",
        "locked": "false",
        "last_seen": "2024-01-01T00:00:00Z",
        "first_seen": "2024-01-01T00:00:00Z"
    },
    "success": true,
    "message": "User retrieved successfully"
}
```

### Register User
Registers a new user.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "registerUser",
    "data": {
        "username": "newuser",
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "token": "user_token",
        "first_ip": "127.0.0.1",
        "last_ip": "127.0.0.1",
    }
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "registerUser",
    "data": {
        "id": 2,
        "username": "newuser",
        "first_name": "New",
        "last_name": "User",
        "email": "newuser@example.com",
        "avatar": "https://www.gravatar.com/avatar",
        "credits": 0,
        "background": "https://cdn.mythical.systems/background.gif",
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "token": "user_token",
        "role": 1,
        "first_ip": "127.0.0.1",
        "last_ip": "127.0.0.1",
        "banned": "NO",
        "verified": "false",
        "support_pin": null,
        "2fa_enabled": "false",
        "2fa_key": null,
        "2fa_blocked": "false",
        "discord_id": null,
        "github_id": null,
        "github_username": null,
        "github_email": null,
        "github_linked": "false",
        "discord_username": null,
        "discord_global_name": null,
        "discord_email": null,
        "discord_linked": "false",
        "deleted": "false",
        "locked": "false",
        "last_seen": "2024-01-03T00:00:00Z",
        "first_seen": "2024-01-03T00:00:00Z"
    },
    "success": true,
    "message": "User registered successfully"
}
```

### Update User
Updates an existing user's information.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "updateUser",
    "data": {
        "id": 1,
        "first_name": "Updated",
        "last_name": "Name",
        "email": "updated@example.com",
        "credits": 100,
        "verified": "true"
    }
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "updateUser",
    "data": {
        "id": 1,
        "username": "user1",
        "first_name": "Updated",
        "last_name": "Name",
        "email": "updated@example.com",
        "avatar": "https://www.gravatar.com/avatar",
        "credits": 100,
        "background": "https://cdn.mythical.systems/background.gif",
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "token": "user_token",
        "role": 1,
        "first_ip": "127.0.0.1",
        "last_ip": "127.0.0.1",
        "banned": "NO",
        "verified": "true",
        "support_pin": "1234",
        "2fa_enabled": "false",
        "2fa_key": null,
        "2fa_blocked": "false",
        "discord_id": null,
        "github_id": null,
        "github_username": null,
        "github_email": null,
        "github_linked": "false",
        "discord_username": null,
        "discord_global_name": null,
        "discord_email": null,
        "discord_linked": "false",
        "deleted": "false",
        "locked": "false",
        "last_seen": "2024-01-01T00:00:00Z",
        "first_seen": "2024-01-01T00:00:00Z"
    },
    "success": true,
    "message": "User updated successfully"
}
```

## User Activity Operations

### List Activities
Retrieves a list of all user activities.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listActivities"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "listActivities",
    "data": [
        {
            "id": 1,
            "user": "550e8400-e29b-41d4-a716-446655440000",
            "action": "User logged in",
            "ip_address": "127.0.0.1",
            "deleted": "false",
            "locked": "false",
            "date": "2024-01-01T00:00:00Z",
            "context": "Web interface"
        }
    ],
    "success": true,
    "message": "Activities retrieved successfully"
}
```

### Get Activities by User
Retrieves all activities for a specific user.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getActivitiesByUser",
    "data": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getActivitiesByUser",
    "data": [
        {
            "id": 1,
            "user": "550e8400-e29b-41d4-a716-446655440000",
            "action": "User logged in",
            "ip_address": "127.0.0.1",
            "deleted": "false",
            "locked": "false",
            "date": "2024-01-01T00:00:00Z",
            "context": "Web interface"
        }
    ],
    "success": true,
    "message": "User activities retrieved successfully"
}
```

### Add Activity
Adds a new user activity.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "addActivity",
    "data": {
        "user": "550e8400-e29b-41d4-a716-446655440000",
        "action": "User logged in",
        "ip_address": "127.0.0.1",
        "context": "Web interface"
    }
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "addActivity",
    "data": {
        "id": 1,
        "user": "550e8400-e29b-41d4-a716-446655440000",
        "action": "User logged in",
        "ip_address": "127.0.0.1",
        "deleted": "false",
        "locked": "false",
        "date": "2024-01-01T00:00:00Z",
        "context": "Web interface"
    },
    "success": true,
    "message": "Activity added successfully"
}
```

### Get Activity by ID
Retrieves a specific activity by its ID.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getActivityById",
    "data": 1
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "getActivityById",
    "data": {
        "id": 1,
        "user": "550e8400-e29b-41d4-a716-446655440000",
        "action": "User logged in",
        "ip_address": "127.0.0.1",
        "deleted": "false",
        "locked": "false",
        "date": "2024-01-01T00:00:00Z",
        "context": "Web interface"
    },
    "success": true,
    "message": "Activity retrieved successfully"
}
```

### Update Activity
Updates an existing activity.

**Request:**
```json
{
    "type": "database",
    "content": null,
    "operation": "updateActivity",
    "data": {
        "id": 1,
        "action": "User logged out",
        "context": "Mobile app"
    }
}
```

**Response:**
```json
{
    "type": "database",
    "content": null,
    "operation": "updateActivity",
    "data": {
        "id": 1,
        "user": "550e8400-e29b-41d4-a716-446655440000",
        "action": "User logged out",
        "ip_address": "127.0.0.1",
        "deleted": "false",
        "locked": "false",
        "date": "2024-01-01T00:00:00Z",
        "context": "Mobile app"
    },
    "success": true,
    "message": "Activity updated successfully"
}
```

## Error Responses
All operations may return an error response in the following format:
```json
{
    "type": "database",
    "content": null,
    "operation": "operation_name",
    "success": false,
    "message": "Error message describing what went wrong"
}
```

Common error messages:
- "Unknown database operation: operation_name"
- "Required parameter missing: parameter_name"
- "Error executing operation: error_message"
- "User not found"
- "Username already exists"
- "Email already exists"
- "Invalid user data"
- "UUID already exists"
- "Activity not found"
- "Invalid activity data"

## Important Notes
1. All timestamps are in ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ)
2. The settings system uses a local cache that refreshes every 5 seconds
3. All responses include a `success` boolean field indicating if the operation was successful
4. All responses include a `message` field with additional information about the operation result
5. The `mccloudadmin_users` table has a unique constraint on the `uuid` field
6. Enum fields (`verified`, `2fa_enabled`, `2fa_blocked`, `github_linked`, `discord_linked`, `deleted`, `locked`) can only have values 'true' or 'false'
7. The `role` field is a foreign key referencing `mccloudadmin_roles.id`
8. The `user` field in `mccloudadmin_users_activities` is a foreign key referencing `mccloudadmin_users.uuid`
9. The `deleted` and `locked` fields in `mccloudadmin_users_activities` are enums that can only have values 'true' or 'false'
10. The `date` field in `mccloudadmin_users_activities` is automatically set to the current timestamp when a new activity is added 