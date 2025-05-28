package systems.mythical.cloudcore.core;

import java.util.Arrays;
import java.util.List;

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
        public static final String ADMIN_INFO = "cloudcore.admin.info";

        
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

        // Global App Settings
        public static final String GLOBAL_APP_NAME = "app_name";
        public static final String GLOBAL_APP_LOGO = "app_logo";
        public static final String GLOBAL_APP_URL = "app_url";
        public static final String GLOBAL_APP_VERSION = "app_version";
        public static final String GLOBAL_APP_PROFILE_ENABLED = "allow_public_profiles";
        public static final String GLOBAL_APP_ALLOW_TICKETS = "allow_tickets";
        public static final String GLOBAL_WEBSITE_URL = "website_url";
        public static final String GLOBAL_STATUS_PAGE_URL = "status_page_url";
        public static final String GLOBAL_DISCORD_INVITE_URL = "discord_invite_url";
        public static final String GLOBAL_TWITTER_URL = "twitter_url";
        public static final String GLOBAL_GITHUB_URL = "github_url";
        public static final String GLOBAL_LINKEDIN_URL = "linkedin_url";
        public static final String GLOBAL_INSTAGRAM_URL = "instagram_url";
        public static final String GLOBAL_YOUTUBE_URL = "youtube_url";
        public static final String GLOBAL_TIKTOK_URL = "tiktok_url";
        public static final String GLOBAL_FACEBOOK_URL = "facebook_url";
        public static final String GLOBAL_REDDIT_URL = "reddit_url";
        public static final String GLOBAL_TELEGRAM_URL = "telegram_url";
        public static final String GLOBAL_WHATSAPP_URL = "whatsapp_url";
        public static final String GLOBAL_ALLOW_CODE_REDEEM = "code_redemption_enabled";

        public static List<String> getGlobalSettings() {
            return Arrays.asList(
                GLOBAL_APP_NAME,
                GLOBAL_APP_LOGO,
                GLOBAL_APP_URL,
                GLOBAL_APP_VERSION,
                GLOBAL_APP_PROFILE_ENABLED,
                GLOBAL_APP_ALLOW_TICKETS,
                GLOBAL_WEBSITE_URL,
                GLOBAL_STATUS_PAGE_URL,
                GLOBAL_DISCORD_INVITE_URL,
                GLOBAL_TWITTER_URL,
                GLOBAL_GITHUB_URL,
                GLOBAL_LINKEDIN_URL,
                GLOBAL_INSTAGRAM_URL,
                GLOBAL_YOUTUBE_URL,
                GLOBAL_TIKTOK_URL,
                GLOBAL_FACEBOOK_URL,
                GLOBAL_REDDIT_URL,
                GLOBAL_TELEGRAM_URL,
                GLOBAL_WHATSAPP_URL,
                GLOBAL_ALLOW_CODE_REDEEM
            );
        }
    }

    /**
     * Message keys used across the plugin
     */
    public static final class Messages {
        // Connection messages
        public static final String CONNECTION_BLOCKED_VPN = "connection_blocked_vpn";
        public static final String CONNECTION_BLOCKED_ALTS = "connection_blocked_alts";
        public static final String CONNECTION_BLOCKED_MAINTENANCE = "connection_blocked_maintenance";

        // Profile command messages
        public static final String PROFILE_USAGE = "profile.usage";
        public static final String PROFILE_NOT_FOUND = "profile.not_found";
        public static final String PROFILE_LINK = "profile.link";

        // Information command messages
        public static final String INFORMATION_USAGE_OTHER = "information.usage.other";
        public static final String INFORMATION_USAGE_SELF = "information.usage.self";
        public static final String INFORMATION_NOT_FOUND = "information.not_found";
        public static final String INFORMATION_NAME = "information.name";
        public static final String INFORMATION_RANK = "information.rank";
        public static final String INFORMATION_SERVER = "information.server";
        public static final String INFORMATION_ONLINE = "information.online";
        public static final String INFORMATION_VERSION = "information.version";
        public static final String INFORMATION_FIRST_LOGIN = "information.first_login";
        public static final String INFORMATION_LAST_LOGIN = "information.last_login";
        public static final String INFORMATION_GITHUB = "information.github";
        public static final String INFORMATION_DISCORD = "information.discord";
        public static final String INFORMATION_BAN_STATUS = "information.ban_status";
        public static final String INFORMATION_MUTE_STATUS = "information.mute_status";
        public static final String INFORMATION_OTHER_ACCOUNTS = "information.other_accounts";
        public static final String INFORMATION_OTHER_ACCOUNT = "information.other_account";
        public static final String INFORMATION_IP = "information.ip";
        public static final String INFORMATION_CREDITS = "information.credits";
        public static final String INFORMATION_HEADER = "information.header";
        public static final String INFORMATION_VERIFIED = "information.verified";
        public static final String INFORMATION_SUPPORT_PIN = "information.support_pin";
        
        
        // Status messages
        public static final String STATUS_YES = "status.yes";
        public static final String STATUS_NO = "status.no";
        public static final String STATUS_ONLINE = "status.online";
        public static final String STATUS_OFFLINE = "status.offline";
        public static final String STATUS_BANNED = "status.banned";
        public static final String STATUS_NOT_BANNED = "status.not_banned";
        public static final String STATUS_MUTED = "status.muted";
        public static final String STATUS_NOT_MUTED = "status.not_muted";

        // Panel messages
        public static final String PANEL_PLAYERS_ONLY = "panel.players_only";
        public static final String PANEL_NO_ACCOUNT = "panel.no_account";
        public static final String PANEL_LOGIN_URL = "panel.login_url";
        public static final String PANEL_RESET_MSG = "panel.reset_msg";
        public static final String PANEL_USAGE = "panel.usage";

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

        // Chatlog command messages
        public static final String CHATLOG_LINK = "chatlog.link";
        public static final String CHATLOG_USAGE = "chatlog.usage";
        public static final String CHATLOG_NOT_FOUND = "chatlog.not_found";
        public static final String CHATLOG_NO_MESSAGES = "chatlog.no_messages";
    }
} 