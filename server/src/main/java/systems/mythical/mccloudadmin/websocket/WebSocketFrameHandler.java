package systems.mythical.mccloudadmin.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.model.WebSocketMessage;
import systems.mythical.mccloudadmin.service.DatabaseService;
import systems.mythical.mccloudadmin.service.SettingsService;
import systems.mythical.mccloudadmin.service.UserService;
import systems.mythical.mccloudadmin.service.UserActivityService;
import systems.mythical.mccloudadmin.websocket.message.DatabaseMessage;

import java.util.List;
import java.util.Map;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DatabaseService databaseService = DatabaseService.getInstance();
    private final SettingsService settingsService = SettingsService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final UserActivityService userActivityService = UserActivityService.getInstance();

    public WebSocketFrameHandler() {
        // Initialize any other necessary fields
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            String messageText = frame.text();
            logger.debug("Received raw message: {}", messageText);

            if (messageText.trim().isEmpty()) {
                sendError(ctx, "Empty message received");
                return;
            }

            try {
                // Try to parse as DatabaseMessage first
                try {
                    DatabaseMessage dbMessage = objectMapper.readValue(messageText, DatabaseMessage.class);
                    handleDatabaseMessage(ctx, dbMessage);
                    return;
                } catch (JsonProcessingException e) {
                    // If not a DatabaseMessage, try as WebSocketMessage
                    WebSocketMessage message = objectMapper.readValue(messageText, WebSocketMessage.class);
                    logger.info("Received message - Type: {}, Content: {}", message.getType(), message.getContent());
                    
                    // Process the message and send response
                    WebSocketMessage response = new WebSocketMessage("response", "Message received: " + message.getContent());
                    sendMessage(ctx, response);
                }
            } catch (JsonProcessingException e) {
                logger.warn("Invalid JSON format: {}", messageText);
                sendError(ctx, "Invalid message format. Expected JSON with 'type' and 'content' fields");
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);
            sendError(ctx, "Internal server error");
            ctx.close();
        }
    }

    private void handleDatabaseMessage(ChannelHandlerContext ctx, DatabaseMessage message) {
        switch (message.getOperation()) {
            case "listTables":
                handleListTables(ctx);
                break;
            case "getTableStructure":
                if (message.getData() instanceof String) {
                    handleGetTableStructure(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Table name is required");
                }
                break;
            case "getSetting":
                if (message.getData() instanceof String) {
                    handleGetSetting(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Setting name is required");
                }
                break;
            case "setSetting":
                if (message.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    if (data.containsKey("name") && data.containsKey("value")) {
                        handleSetSetting(ctx, (String) data.get("name"), String.valueOf(data.get("value")));
                    } else {
                        sendError(ctx, message.getOperation(), "Setting name and value are required");
                    }
                } else {
                    sendError(ctx, message.getOperation(), "Invalid data format");
                }
                break;
            case "getAllSettings":
                handleGetAllSettings(ctx);
                break;
            case "deleteSetting":
                if (message.getData() instanceof String) {
                    handleDeleteSetting(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Setting name is required");
                }
                break;
            case "listUsers":
                handleListUsers(ctx);
                break;
            case "getUserById":
                if (message.getData() instanceof Integer) {
                    handleGetUserById(ctx, (Integer) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "User ID is required");
                }
                break;
            case "getUserByEmail":
                if (message.getData() instanceof String) {
                    handleGetUserByEmail(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Email is required");
                }
                break;
            case "getUserByUsername":
                if (message.getData() instanceof String) {
                    handleGetUserByUsername(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Username is required");
                }
                break;
            case "getUserByUuid":
                if (message.getData() instanceof String) {
                    handleGetUserByUuid(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "UUID is required");
                }
                break;
            case "registerUser":
                if (message.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    handleRegisterUser(ctx, data);
                } else {
                    sendError(ctx, message.getOperation(), "Invalid user data");
                }
                break;
            case "updateUser":
                if (message.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    if (data.containsKey("id")) {
                        handleUpdateUser(ctx, (Integer) data.get("id"), data);
                    } else {
                        sendError(ctx, message.getOperation(), "User ID is required");
                    }
                } else {
                    sendError(ctx, message.getOperation(), "Invalid user data");
                }
                break;
            case "listActivities":
                handleListActivities(ctx);
                break;
            case "getActivitiesByUser":
                if (message.getData() instanceof String) {
                    handleGetActivitiesByUser(ctx, (String) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "User UUID is required");
                }
                break;
            case "addActivity":
                if (message.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    if (data.containsKey("user") && data.containsKey("action") && data.containsKey("ip_address")) {
                        handleAddActivity(ctx, data);
                    } else {
                        sendError(ctx, message.getOperation(), "Missing required fields: user, action, ip_address");
                    }
                } else {
                    sendError(ctx, message.getOperation(), "Invalid data format");
                }
                break;
            case "getActivityById":
                if (message.getData() instanceof Integer) {
                    handleGetActivityById(ctx, (Integer) message.getData());
                } else {
                    sendError(ctx, message.getOperation(), "Activity ID is required");
                }
                break;
            case "updateActivity":
                if (message.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    if (data.containsKey("id")) {
                        handleUpdateActivity(ctx, (Integer) data.get("id"), data);
                    } else {
                        sendError(ctx, message.getOperation(), "Activity ID is required");
                    }
                } else {
                    sendError(ctx, message.getOperation(), "Invalid data format");
                }
                break;
            default:
                sendError(ctx, message.getOperation(), "Unknown database operation: " + message.getOperation());
                break;
        }
    }

    private void handleListTables(ChannelHandlerContext ctx) {
        try {
            List<Map<String, Object>> tables = databaseService.listTables();
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("listTables");
            response.setData(tables);
            response.setSuccess(true);
            response.setMessage("Tables retrieved successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error listing tables: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("listTables");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error listing tables: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetTableStructure(ChannelHandlerContext ctx, String tableName) {
        try {
            List<Map<String, Object>> structure = databaseService.getTableStructure(tableName);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getTableStructure");
            response.setTableName(tableName);
            response.setData(structure);
            response.setSuccess(true);
            response.setMessage("Table structure retrieved successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting table structure: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getTableStructure");
            errorResponse.setTableName(tableName);
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting table structure: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetSetting(ChannelHandlerContext ctx, String settingName) {
        try {
            String value = settingsService.getSetting(settingName);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getSetting");
            response.setSettingName(settingName);
            response.setData(value);
            response.setSuccess(true);
            response.setMessage(value != null ? "Setting retrieved successfully" : "Setting not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting setting: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getSetting");
            errorResponse.setSettingName(settingName);
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting setting: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleSetSetting(ChannelHandlerContext ctx, String settingName, String settingValue) {
        try {
            settingsService.setSetting(settingName, settingValue);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("setSetting");
            response.setSettingName(settingName);
            response.setSettingValue(settingValue);
            response.setSuccess(true);
            response.setMessage("Setting updated successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error setting setting: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("setSetting");
            errorResponse.setSettingName(settingName);
            errorResponse.setSettingValue(settingValue);
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error setting setting: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetAllSettings(ChannelHandlerContext ctx) {
        try {
            Map<String, String> settings = settingsService.getAllSettings();
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getAllSettings");
            response.setData(settings);
            response.setSuccess(true);
            response.setMessage("Settings retrieved successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting all settings: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getAllSettings");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting all settings: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleDeleteSetting(ChannelHandlerContext ctx, String settingName) {
        try {
            settingsService.deleteSetting(settingName);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("deleteSetting");
            response.setSettingName(settingName);
            response.setSuccess(true);
            response.setMessage("Setting deleted successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error deleting setting: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("deleteSetting");
            errorResponse.setSettingName(settingName);
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error deleting setting: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleListUsers(ChannelHandlerContext ctx) {
        try {
            List<Map<String, Object>> users = userService.listUsers();
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("listUsers");
            response.setData(users);
            response.setSuccess(true);
            response.setMessage("Users retrieved successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("listUsers");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error listing users: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetUserById(ChannelHandlerContext ctx, int id) {
        try {
            Map<String, Object> user = userService.getUserById(id);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getUserById");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage(user != null ? "User retrieved successfully" : "User not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting user by id: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getUserById");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetUserByEmail(ChannelHandlerContext ctx, String email) {
        try {
            Map<String, Object> user = userService.getUserByEmail(email);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getUserByEmail");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage(user != null ? "User retrieved successfully" : "User not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting user by email: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getUserByEmail");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetUserByUsername(ChannelHandlerContext ctx, String username) {
        try {
            Map<String, Object> user = userService.getUserByUsername(username);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getUserByUsername");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage(user != null ? "User retrieved successfully" : "User not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting user by username: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getUserByUsername");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleGetUserByUuid(ChannelHandlerContext ctx, String uuid) {
        try {
            Map<String, Object> user = userService.getUserByUuid(uuid);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("getUserByUuid");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage(user != null ? "User retrieved successfully" : "User not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error getting user by uuid: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("getUserByUuid");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error getting user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleRegisterUser(ChannelHandlerContext ctx, Map<String, Object> userData) {
        try {
            Map<String, Object> user = userService.registerUser(userData);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("registerUser");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage("User registered successfully");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("registerUser");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error registering user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleUpdateUser(ChannelHandlerContext ctx, int id, Map<String, Object> userData) {
        try {
            Map<String, Object> user = userService.updateUser(id, userData);
            DatabaseMessage response = new DatabaseMessage();
            response.setOperation("updateUser");
            response.setData(user);
            response.setSuccess(true);
            response.setMessage(user != null ? "User updated successfully" : "User not found");
            sendMessage(ctx, response);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            DatabaseMessage errorResponse = new DatabaseMessage();
            errorResponse.setOperation("updateUser");
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error updating user: " + e.getMessage());
            sendMessage(ctx, errorResponse);
        }
    }

    private void handleListActivities(ChannelHandlerContext ctx) {
        try {
            List<Map<String, Object>> activities = userActivityService.listActivities();
            DatabaseMessage response = new DatabaseMessage();
            response.setType("database");
            response.setOperation("listActivities");
            response.setData(activities);
            response.setSuccess(true);
            response.setMessage("Activities retrieved successfully");
            ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
        } catch (Exception e) {
            logger.error("Error listing activities: {}", e.getMessage());
            sendError(ctx, "listActivities", "Failed to list activities: " + e.getMessage());
        }
    }

    private void handleGetActivitiesByUser(ChannelHandlerContext ctx, String userUuid) {
        try {
            List<Map<String, Object>> activities = userActivityService.getActivitiesByUser(userUuid);
            DatabaseMessage response = new DatabaseMessage();
            response.setType("database");
            response.setOperation("getActivitiesByUser");
            response.setData(activities);
            response.setSuccess(true);
            response.setMessage("User activities retrieved successfully");
            ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
        } catch (Exception e) {
            logger.error("Error getting activities for user {}: {}", userUuid, e.getMessage());
            sendError(ctx, "getActivitiesByUser", "Failed to get user activities: " + e.getMessage());
        }
    }

    private void handleAddActivity(ChannelHandlerContext ctx, Map<String, Object> data) {
        try {
            String userUuid = (String) data.get("user");
            String action = (String) data.get("action");
            String ipAddress = (String) data.get("ip_address");
            String context = (String) data.getOrDefault("context", "None");

            Map<String, Object> activity = userActivityService.addActivity(userUuid, action, ipAddress, context);
            DatabaseMessage response = new DatabaseMessage();
            response.setType("database");
            response.setOperation("addActivity");
            response.setData(activity);
            response.setSuccess(true);
            response.setMessage("Activity added successfully");
            ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
        } catch (Exception e) {
            logger.error("Error adding activity: {}", e.getMessage());
            sendError(ctx, "addActivity", "Failed to add activity: " + e.getMessage());
        }
    }

    private void handleGetActivityById(ChannelHandlerContext ctx, int id) {
        try {
            Map<String, Object> activity = userActivityService.getActivityById(id);
            if (activity != null) {
                DatabaseMessage response = new DatabaseMessage();
                response.setType("database");
                response.setOperation("getActivityById");
                response.setData(activity);
                response.setSuccess(true);
                response.setMessage("Activity retrieved successfully");
                ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
            } else {
                sendError(ctx, "getActivityById", "Activity not found");
            }
        } catch (Exception e) {
            logger.error("Error getting activity by id {}: {}", id, e.getMessage());
            sendError(ctx, "getActivityById", "Failed to get activity: " + e.getMessage());
        }
    }

    private void handleUpdateActivity(ChannelHandlerContext ctx, int id, Map<String, Object> activityData) {
        try {
            Map<String, Object> activity = userActivityService.updateActivity(id, activityData);
            if (activity != null) {
                DatabaseMessage response = new DatabaseMessage();
                response.setType("database");
                response.setOperation("updateActivity");
                response.setData(activity);
                response.setSuccess(true);
                response.setMessage("Activity updated successfully");
                ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
            } else {
                sendError(ctx, "updateActivity", "Activity not found");
            }
        } catch (Exception e) {
            logger.error("Error updating activity {}: {}", id, e.getMessage());
            sendError(ctx, "updateActivity", "Failed to update activity: " + e.getMessage());
        }
    }

    private void sendMessage(ChannelHandlerContext ctx, Object message) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(message);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(jsonResponse));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing response", e);
            sendError(ctx, "Error processing response");
        }
    }

    private void sendError(ChannelHandlerContext ctx, String operation, String errorMessage) {
        DatabaseMessage response = new DatabaseMessage();
        response.setType("database");
        response.setOperation(operation);
        response.setSuccess(false);
        response.setMessage(errorMessage);
        ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
    }

    private void sendError(ChannelHandlerContext ctx, String errorMessage) {
        DatabaseMessage response = new DatabaseMessage();
        response.setType("database");
        response.setSuccess(false);
        response.setMessage(errorMessage);
        ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("New client connected from {}", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("Client disconnected from {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("WebSocket error from {}: {}", ctx.channel().remoteAddress(), cause.getMessage());
        sendError(ctx, "Connection error occurred");
        ctx.close();
    }
} 