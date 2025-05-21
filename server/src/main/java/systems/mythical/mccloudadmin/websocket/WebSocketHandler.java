package systems.mythical.mccloudadmin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.service.DatabaseService;
import systems.mythical.mccloudadmin.service.SettingsService;
import systems.mythical.mccloudadmin.websocket.message.*;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
public class WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DatabaseService databaseService = DatabaseService.getInstance();
    private static final SettingsService settingsService = SettingsService.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        logger.info("New WebSocket connection established: {}", session.getId());
        sessions.put(session, null);
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("WebSocket connection closed: {}", session.getId());
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            BaseMessage baseMessage = objectMapper.readValue(message, BaseMessage.class);
            
            switch (baseMessage.getType()) {
                case "database":
                    handleDatabaseMessage(objectMapper.readValue(message, DatabaseMessage.class), session);
                    break;
                default:
                    logger.warn("Unknown message type: {}", baseMessage.getType());
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    private void handleDatabaseMessage(DatabaseMessage message, Session session) {
        try {
            switch (message.getOperation()) {
                case "listTables":
                    handleListTables(session);
                    break;
                case "getTableStructure":
                    if (message.getTableName() != null) {
                        handleGetTableStructure(message.getTableName(), session);
                    } else {
                        sendError(session, "Table name is required for getTableStructure operation");
                    }
                    break;
                case "getSetting":
                    if (message.getSettingName() != null) {
                        handleGetSetting(message.getSettingName(), session);
                    } else {
                        sendError(session, "Setting name is required for getSetting operation");
                    }
                    break;
                case "setSetting":
                    if (message.getSettingName() != null && message.getSettingValue() != null) {
                        handleSetSetting(message.getSettingName(), message.getSettingValue(), session);
                    } else {
                        sendError(session, "Setting name and value are required for setSetting operation");
                    }
                    break;
                case "getAllSettings":
                    handleGetAllSettings(session);
                    break;
                case "deleteSetting":
                    if (message.getSettingName() != null) {
                        handleDeleteSetting(message.getSettingName(), session);
                    } else {
                        sendError(session, "Setting name is required for deleteSetting operation");
                    }
                    break;
                default:
                    sendError(session, "Unknown database operation: " + message.getOperation());
            }
        } catch (Exception e) {
            logger.error("Error handling database message: {}", e.getMessage());
            sendError(session, "Error handling database operation: " + e.getMessage());
        }
    }

    private void handleListTables(Session session) throws IOException {
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("listTables");
        response.setData(databaseService.listTables());
        sendMessage(session, response);
    }

    private void handleGetTableStructure(String tableName, Session session) throws IOException {
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("getTableStructure");
        response.setTableName(tableName);
        response.setData(databaseService.getTableStructure(tableName));
        sendMessage(session, response);
    }

    private void handleGetSetting(String settingName, Session session) throws IOException {
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("getSetting");
        response.setSettingName(settingName);
        response.setData(settingsService.getSetting(settingName));
        sendMessage(session, response);
    }

    private void handleSetSetting(String settingName, String settingValue, Session session) throws IOException {
        settingsService.setSetting(settingName, settingValue);
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("setSetting");
        response.setSettingName(settingName);
        response.setSettingValue(settingValue);
        sendMessage(session, response);
    }

    private void handleGetAllSettings(Session session) throws IOException {
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("getAllSettings");
        response.setData(settingsService.getAllSettings());
        sendMessage(session, response);
    }

    private void handleDeleteSetting(String settingName, Session session) throws IOException {
        settingsService.deleteSetting(settingName);
        DatabaseMessage response = new DatabaseMessage();
        response.setOperation("deleteSetting");
        response.setSettingName(settingName);
        sendMessage(session, response);
    }

    private void sendError(Session session, String errorMessage) {
        try {
            BaseMessage error = new BaseMessage("error", errorMessage);
            sendMessage(session, error);
        } catch (IOException e) {
            logger.error("Error sending error message: {}", e.getMessage());
        }
    }

    private void sendMessage(Session session, BaseMessage message) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        session.getBasicRemote().sendText(jsonMessage);
    }
} 