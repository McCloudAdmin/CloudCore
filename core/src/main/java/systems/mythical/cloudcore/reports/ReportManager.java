package systems.mythical.cloudcore.reports;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.messages.MessageManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

public class ReportManager {
    private static ReportManager instance;
    private final DatabaseManager databaseManager;
    @SuppressWarnings("unused")
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private final CloudLogger cloudLogger;
    private final Map<UUID, Long> reportCooldowns;

    // Settings
    private static final CommonSettings.IntegerSetting REPORT_COOLDOWN = new CommonSettings.IntegerSetting("report_cooldown", 120);

    private ReportManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.messageManager = MessageManager.getInstance(databaseManager, platformLogger);
        this.cloudSettings = CloudSettings.getInstance(databaseManager, platformLogger);
        this.cloudLogger = CloudLoggerFactory.get();
        this.reportCooldowns = new ConcurrentHashMap<>();
    }

    public static ReportManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ReportManager(databaseManager, logger);
        }
        return instance;
    }


    public int getReportCooldown() {
        int value = REPORT_COOLDOWN.parseValue(cloudSettings.getSetting(REPORT_COOLDOWN.getName()));
        if (value < 0) {
            return 0;
        }
        return value;
    }

    public boolean canReport(UUID reporter) {
        Long lastReport = reportCooldowns.get(reporter);
        if (lastReport == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownTime = getReportCooldown() * 1000L; // Convert to milliseconds
        return (currentTime - lastReport) >= cooldownTime;
    }

    public long getRemainingCooldown(UUID reporter) {
        Long lastReport = reportCooldowns.get(reporter);
        if (lastReport == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownTime = getReportCooldown() * 1000L;
        long remaining = cooldownTime - (currentTime - lastReport);
        return Math.max(0, remaining / 1000); // Convert back to seconds
    }

    public boolean createReport(UUID reporter, UUID reported, String reason) {
        if (!canReport(reporter)) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_reports (reported_by, reported_to, reason) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, reporter.toString());
                stmt.setString(2, reported.toString());
                stmt.setString(3, reason);
                stmt.executeUpdate();

                // Update cooldown
                reportCooldowns.put(reporter, System.currentTimeMillis());
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error creating report: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getReport(int reportId) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_reports WHERE id = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reportId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> report = new HashMap<>();
                        report.put("id", rs.getInt("id"));
                        report.put("reported_by", rs.getString("reported_by"));
                        report.put("reported_to", rs.getString("reported_to"));
                        report.put("reason", rs.getString("reason"));
                        report.put("status", rs.getString("status"));
                        report.put("created_at", rs.getTimestamp("created_at"));
                        report.put("updated_at", rs.getTimestamp("updated_at"));
                        report.put("locked", rs.getString("locked"));
                        return report;
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting report: " + e.getMessage());
        }
        return null;
    }

    public boolean updateReportStatus(int reportId, String status) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_reports SET status = ? WHERE id = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, status);
                stmt.setInt(2, reportId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error updating report status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReport(int reportId) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_reports SET deleted = 'true' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reportId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error deleting report: " + e.getMessage());
            return false;
        }
    }
} 