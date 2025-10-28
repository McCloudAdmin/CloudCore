package systems.mythical.cloudcore.firewall;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProxyListManager {
    private static ProxyListManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;

    private ProxyListManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public static ProxyListManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ProxyListManager(databaseManager, logger);
        }
        return instance;
    }

    public boolean isProxyIP(String ip) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT id FROM mccloudadmin_proxylist WHERE ip = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, ip);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error checking proxy IP: " + e.getMessage());
            return false;
        }
    }

    public List<ProxyList> getAllProxyIPs() {
        List<ProxyList> proxies = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_proxylist WHERE deleted = 'false' ORDER BY updated_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        proxies.add(mapResultSetToProxy(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting proxy list: " + e.getMessage());
        }
        return proxies;
    }

    private ProxyList mapResultSetToProxy(ResultSet rs) throws SQLException {
        ProxyList proxy = new ProxyList();
        proxy.setId(rs.getInt("id"));
        proxy.setIp(rs.getString("ip"));
        proxy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        proxy.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        proxy.setDeleted(rs.getString("deleted").equals("true"));
        proxy.setLocked(rs.getString("locked").equals("true"));
        return proxy;
    }
}
