package systems.mythical.cloudcore.core;

/**
 * Centralized class for storing all permission nodes and config nodes used across CloudCore
 */
public final class CloudCoreConstants {
    private CloudCoreConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Permission nodes used across the plugin
     */
    public static final class Permissions {
        // VPN related permissions
        public static final String VPN_BYPASS = "cloudcore.vpn.bypass";
        public static final String ALTS_BYPASS = "cloudcore.alts.bypass";

        // Chat related permissions
        public static final String CHAT_BYPASS = "cloudcore.chat.bypass";
        public static final String CHAT_COLOR = "cloudcore.chat.color";

        // Command related permissions
        public static final String COMMAND_BYPASS = "cloudcore.command.bypass";
        public static final String CONSOLE_EXECUTE = "cloudcore.console.execute";
        public static final String REPORT_USE = "cloudcore.report";
        public static final String REPORT_NOTIFY = "cloudcore.report.notify";
        public static final String ALERT_USE = "cloudcore.alert";
        public static final String MAINTENANCE_BYPASS = "cloudcore.maintenance.bypass";
        public static final String MAINTENANCE_TOGGLE = "cloudcore.maintenance.toggle";
        public static final String MAINTENANCE_MANAGE = "cloudcore.maintenance.manage";
        public static final String SETTINGS_MANAGE = "cloudcore.settings.manage";

        // Admin commands
        public static final String ADMIN_RELOAD = "cloudcore.admin.reload";
        public static final String ADMIN_CLEAR_CACHE = "cloudcore.admin.clearcache";
        public static final String ADMIN_VERSION = "cloudcore.admin.version";
    }

    /**
     * Configuration nodes used across the plugin
     */
    public static final class Settings {
        // Firewall settings
        public static final String FIREWALL_ENABLED = "firewall_enabled";
        public static final String FIREWALL_BLOCK_VPN = "firewall_block_vpn";
        public static final String FIREWALL_BLOCK_ALTS = "firewall_block_alts";

        // Logging settings
        public static final String LOG_CHAT = "log_chatlogs";
        public static final String LOG_COMMANDS = "log_command_logs";
        public static final String LOG_JOINS = "log_join_events";

        // Report system settings
        public static final String REPORT_SYSTEM_ENABLED = "report_system_enabled";
        public static final String REPORT_COOLDOWN = "report_cooldown";

        // Maintenance settings
        public static final String MAINTENANCE_MODE = "maintenance_mode";

        // Alert settings
        public static final String ENABLE_ALERT_COMMAND = "enable_alert_command";

        // Proxy console settings
        public static final String ENABLE_CONSOLE_COMMAND = "enable_console_command";
    }

    /**
     * Message keys used across the plugin
     */
    public static final class Messages {
        // Connection messages
        public static final String CONNECTION_BLOCKED_VPN = "connection_blocked_vpn";
        public static final String CONNECTION_BLOCKED_ALTS = "connection_blocked_alts";
        public static final String CONNECTION_BLOCKED_MAINTENANCE = "connection_blocked_maintenance";

        // Report messages
        public static final String REPORT_USAGE = "report.usage";
        public static final String REPORT_PLAYER_NOT_FOUND = "report.player_not_found";
        public static final String REPORT_CANNOT_REPORT_SELF = "report.cannot_report_self";
        public static final String REPORT_COOLDOWN = "report.cooldown";
        public static final String REPORT_SUCCESS = "report.success";
        public static final String REPORT_ERROR = "report.error";
        public static final String REPORT_STAFF_NOTIFICATION = "report.staff_notification";

        // Alert messages
        public static final String ALERT_USAGE = "alert.usage";
        public static final String ALERT_FORMAT = "alert.format";
        public static final String ALERT_SENT = "alert.sent";

        // Proxy console messages
        public static final String CONSOLE_USAGE = "console.usage";
        public static final String CONSOLE_NOT_ALLOWED = "console.not_allowed";
        public static final String CONSOLE_EXECUTED = "console.executed";
        public static final String CONSOLE_PLAYERS_ONLY = "console.players_only";

        // Admin command messages
        public static final String ADMIN_NO_PERMISSION = "admin.no_permission";
        public static final String ADMIN_RELOAD_START = "admin.reload.start";
        public static final String ADMIN_RELOAD_SUCCESS = "admin.reload.success";
        public static final String ADMIN_RELOAD_ERROR = "admin.reload.error";
        public static final String ADMIN_CACHE_CLEARED = "admin.cache_cleared";
        public static final String ADMIN_VERSION_INFO = "admin.version_info";

        // Maintenance messages
        public static final String MAINTENANCE_USAGE = "maintenance.usage";
        public static final String MAINTENANCE_ENABLED = "maintenance.enabled";
        public static final String MAINTENANCE_DISABLED = "maintenance.disabled";
        public static final String MAINTENANCE_PLAYER_ADDED = "maintenance.player_added";
        public static final String MAINTENANCE_PLAYER_REMOVED = "maintenance.player_removed";
        public static final String MAINTENANCE_PLAYER_NOT_FOUND = "maintenance.player_not_found";
        public static final String MAINTENANCE_LIST = "maintenance.list";
        public static final String MAINTENANCE_LIST_EMPTY = "maintenance.list_empty";

        // Settings messages
        public static final String SETTINGS_USAGE = "settings.usage";
        public static final String SETTINGS_SET = "settings.set";
        public static final String SETTINGS_GET = "settings.get";
        public static final String SETTINGS_LIST = "settings.list";
        public static final String SETTINGS_INVALID = "settings.invalid";
    }
} 