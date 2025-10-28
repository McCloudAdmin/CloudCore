package systems.mythical.cloudcore.core;

import java.util.Arrays;
import java.util.List;

/**
 * Centralized class for storing all permission nodes and config nodes used
 * across CloudCore
 */
public final class CloudCoreConstants {
    private CloudCoreConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Permission Nodes Constants
     * Auto-generated from permission_nodes.txt
     */

    /**
     * ⚠️ WARNING: Do not modify this file manually!
     * This file is auto-generated from permission_nodes.txt
     * Use 'php mccloudadmin permissionExport' to regenerate this file
     * Manual modifications will be overwritten on next generation.
     */

    /**
     * Permission nodes used across the plugin
     */
    public final class Permissions {
        // VPN Bypass Permissions
        /** Allows bypassing VPN restrictions */
        public static final String VPN_BYPASS = "cloudcore.vpn.bypass";

        // Alts Bypass Permissions
        /** Allows bypassing alt account restrictions */
        public static final String ALTS_BYPASS = "cloudcore.alts.bypass";

        // Chat Bypass Permissions
        /** Allows bypassing chat restrictions */
        public static final String CHAT_BYPASS = "cloudcore.chat.bypass";

        // Chat Color Permissions
        /** Allows using color in chat */
        public static final String CHAT_COLOR = "cloudcore.chat.color";

        // Command Bypass Permissions
        /** Allows bypassing command restrictions */
        public static final String COMMAND_BYPASS = "cloudcore.command.bypass";

        // Console Execute Permissions
        /** Allows executing console commands */
        public static final String CONSOLE_EXECUTE = "cloudcore.console.execute";

        // Report Use Permissions
        /** Allows using the report system */
        public static final String REPORT_USE = "cloudcore.report";

        // Report Notify Permissions
        /** Receives notifications about reports */
        public static final String REPORT_NOTIFY = "cloudcore.report.notify";

        // Alert Use Permissions
        /** Allows using the alert system */
        public static final String ALERT_USE = "cloudcore.alert";

        // Maintenance Bypass Permissions
        /** Allows access during maintenance */
        public static final String MAINTENANCE_BYPASS = "cloudcore.maintenance.bypass";

        // Maintenance Toggle Permissions
        /** Allows toggling maintenance mode */
        public static final String MAINTENANCE_TOGGLE = "cloudcore.maintenance.toggle";

        // Maintenance Manage Permissions
        /** Allows managing maintenance settings */
        public static final String MAINTENANCE_MANAGE = "cloudcore.maintenance.manage";

        // Admin Reload Permissions
        /** Allows reloading the system */
        public static final String ADMIN_RELOAD = "cloudcore.admin.reload";

        // Admin Clear Cache Permissions
        /** Allows clearing system cache */
        public static final String ADMIN_CLEAR_CACHE = "cloudcore.admin.clearcache";

        // Admin Version Permissions
        /** Allows checking system version */
        public static final String ADMIN_VERSION = "cloudcore.admin.version";

        // Admin Info Permissions
        /** Allows viewing system information */
        public static final String ADMIN_INFO = "cloudcore.admin.info";

        // Settings Manage Permissions
        /** Allows managing system settings */
        public static final String SETTINGS_MANAGE = "cloudcore.settings.manage";

        // Chatlog Command Permissions
        /** Allows accessing chat logs */
        public static final String CHATLOG_COMMAND = "cloudcore.chatlog.command";

        

        // Panel Admin Permissions
        /** Full administrative access to the web panel */
        public static final String PANEL_ADMIN = "cloudcore.webpanel.admin";

        // Users Index Permissions
        /** Access to view user list */
        public static final String PANEL_USERS_INDEX = "cloudcore.webpanel.users.index";

        // Users Edit Permissions
        /** Access to edit users */
        public static final String PANEL_USERS_EDIT = "cloudcore.webpanel.users.edit";

        // Users Delete Permissions
        /** Access to delete users */
        public static final String PANEL_USERS_DELETE = "cloudcore.webpanel.users.delete";

        // Tickets Index Permissions
        /** Access to view tickets */
        public static final String PANEL_TICKETS_INDEX = "cloudcore.webpanel.tickets.index";

        // Tickets Details Permissions
        /** Access to view ticket details */
        public static final String PANEL_TICKETS_DETAILS = "cloudcore.webpanel.tickets.details";

        // Settings Index Permissions
        /** Access to view settings */
        public static final String PANEL_SETTINGS_INDEX = "cloudcore.webpanel.settings.index";

        // Settings Edit Permissions
        /** Access to edit settings */
        public static final String PANEL_SETTINGS_EDIT = "cloudcore.webpanel.settings.edit";

        // Settings Delete Permissions
        /** Access to delete settings */
        public static final String PANEL_SETTINGS_DELETE = "cloudcore.webpanel.settings.delete";

        // Redirect Links Index Permissions
        /** Access to view redirect links */
        public static final String PANEL_REDIRECT_LINKS_INDEX = "cloudcore.webpanel.redirect-links.index";

        // Redirect Links Create Permissions
        /** Access to create redirect links */
        public static final String PANEL_REDIRECT_LINKS_CREATE = "cloudcore.webpanel.redirect-links.create";

        // Redirect Links Edit Permissions
        /** Access to edit redirect links */
        public static final String PANEL_REDIRECT_LINKS_EDIT = "cloudcore.webpanel.redirect-links.edit";

        // Redirect Links Delete Permissions
        /** Access to delete redirect links */
        public static final String PANEL_REDIRECT_LINKS_DELETE = "cloudcore.webpanel.redirect-links.delete";

        // Redeem Codes Index Permissions
        /** Access to view redeem codes */
        public static final String PANEL_REDEEM_CODES_INDEX = "cloudcore.webpanel.redeem-codes.index";

        // Redeem Codes Create Permissions
        /** Access to create redeem codes */
        public static final String PANEL_REDEEM_CODES_CREATE = "cloudcore.webpanel.redeem-codes.create";

        // Redeem Codes Edit Permissions
        /** Access to edit redeem codes */
        public static final String PANEL_REDEEM_CODES_EDIT = "cloudcore.webpanel.redeem-codes.edit";

        // Redeem Codes Delete Permissions
        /** Access to delete redeem codes */
        public static final String PANEL_REDEEM_CODES_DELETE = "cloudcore.webpanel.redeem-codes.delete";

        // Plugins Index Permissions
        /** Access to view plugins */
        public static final String PANEL_PLUGINS_INDEX = "cloudcore.webpanel.plugins.index";

        // Plugins Config Permissions
        /** Access to configure plugins */
        public static final String PANEL_PLUGINS_CONFIG = "cloudcore.webpanel.plugins.config";

        // Mail Templates Index Permissions
        /** Access to view mail templates */
        public static final String PANEL_MAIL_TEMPLATES_INDEX = "cloudcore.webpanel.mail-templates.index";

        // Mail Templates Create Permissions
        /** Access to create mail templates */
        public static final String PANEL_MAIL_TEMPLATES_CREATE = "cloudcore.webpanel.mail-templates.create";

        // Mail Templates Edit Permissions
        /** Access to edit mail templates */
        public static final String PANEL_MAIL_TEMPLATES_EDIT = "cloudcore.webpanel.mail-templates.edit";

        // Mail Templates Delete Permissions
        /** Access to delete mail templates */
        public static final String PANEL_MAIL_TEMPLATES_DELETE = "cloudcore.webpanel.mail-templates.delete";

        // Announcements Index Permissions
        /** Access to view announcements */
        public static final String PANEL_ANNOUNCEMENTS_INDEX = "cloudcore.webpanel.announcements.index";

        // Announcements Create Permissions
        /** Access to create announcements */
        public static final String PANEL_ANNOUNCEMENTS_CREATE = "cloudcore.webpanel.announcements.create";

        // Announcements Edit Permissions
        /** Access to edit announcements */
        public static final String PANEL_ANNOUNCEMENTS_EDIT = "cloudcore.webpanel.announcements.edit";

        // Announcements Delete Permissions
        /** Access to delete announcements */
        public static final String PANEL_ANNOUNCEMENTS_DELETE = "cloudcore.webpanel.announcements.delete";

        // Backups Index Permissions
        /** Access to view backups */
        public static final String PANEL_BACKUPS_INDEX = "cloudcore.webpanel.backups.index";

        // Departments Index Permissions
        /** Access to view departments */
        public static final String PANEL_DEPARTMENTS_INDEX = "cloudcore.webpanel.departments.index";

        // Departments Create Permissions
        /** Access to create departments */
        public static final String PANEL_DEPARTMENTS_CREATE = "cloudcore.webpanel.departments.create";

        // Departments Edit Permissions
        /** Access to edit departments */
        public static final String PANEL_DEPARTMENTS_EDIT = "cloudcore.webpanel.departments.edit";

        // Departments Delete Permissions
        /** Access to delete departments */
        public static final String PANEL_DEPARTMENTS_DELETE = "cloudcore.webpanel.departments.delete";

        // Images Index Permissions
        /** Access to view images */
        public static final String PANEL_IMAGES_INDEX = "cloudcore.webpanel.images.index";

        // Images Create Permissions
        /** Access to create images */
        public static final String PANEL_IMAGES_CREATE = "cloudcore.webpanel.images.create";

        // Images Delete Permissions
        /** Access to delete images */
        public static final String PANEL_IMAGES_DELETE = "cloudcore.webpanel.images.delete";

        // Rules Index Permissions
        /** Access to view rules */
        public static final String PANEL_RULES_INDEX = "cloudcore.webpanel.rules.index";

        // Rules Create Permissions
        /** Access to create rules */
        public static final String PANEL_RULES_CREATE = "cloudcore.webpanel.rules.create";

        // Rules Edit Permissions
        /** Access to edit rules */
        public static final String PANEL_RULES_EDIT = "cloudcore.webpanel.rules.edit";

        // Rules Delete Permissions
        /** Access to delete rules */
        public static final String PANEL_RULES_DELETE = "cloudcore.webpanel.rules.delete";

        // Pages Index Permissions
        /** Access to view pages */
        public static final String PANEL_PAGES_INDEX = "cloudcore.webpanel.pages.index";

        // Widget Overview Permissions
        /** Access to overview widget */
        public static final String PANEL_WIDGET_OVERVIEW = "cloudcore.webpanel.widget.overview";

        // Widget At a Glance Permissions
        /** Access to at-a-glance widget */
        public static final String PANEL_WIDGET_AT_A_GLANCE = "cloudcore.webpanel.widget.at-a-glance";

        // Widget Activity Permissions
        /** Access to activity widget */
        public static final String PANEL_WIDGET_ACTIVITY = "cloudcore.webpanel.widget.activity";

        // Widget Quick Actions Permissions
        /** Access to quick actions widget */
        public static final String PANEL_WIDGET_QUICK_ACTIONS = "cloudcore.webpanel.widget.quick-actions";

        // Widget Support Permissions
        /** Access to support widget */
        public static final String PANEL_WIDGET_SUPPORT = "cloudcore.webpanel.widget.support";

        // Widget Logs Permissions
        /** Access to logs widget */
        public static final String PANEL_WIDGET_LOGS = "cloudcore.webpanel.widget.logs";

        // Applications Index Permissions
        /** Access to view applications */
        public static final String PANEL_APPLICATIONS_INDEX = "cloudcore.webpanel.applications.index";

        // Applications View Permissions
        /** Access to view application details */
        public static final String PANEL_APPLICATIONS_VIEW = "cloudcore.webpanel.applications.view";

        // Applications Review Permissions
        /** Access to review applications */
        public static final String PANEL_APPLICATIONS_REVIEW = "cloudcore.webpanel.applications.review";

        // Applications Delete Permissions
        /** Access to delete applications */
        public static final String PANEL_APPLICATIONS_DELETE = "cloudcore.webpanel.applications.delete";

        // Applications Positions Manage Permissions
        /** Access to manage application positions */
        public static final String PANEL_APPLICATIONS_POSITIONS_MANAGE = "cloudcore.webpanel.applications.positions.manage";

        // Applications Questions Manage Permissions
        /** Access to manage application questions */
        public static final String PANEL_APPLICATIONS_QUESTIONS_MANAGE = "cloudcore.webpanel.applications.questions.manage";

        // Reports Index Permissions
        /** Access to view reports */
        public static final String PANEL_REPORTS_INDEX = "cloudcore.webpanel.reports.index";

        // Reports View Permissions
        /** Access to view report details */
        public static final String PANEL_REPORTS_VIEW = "cloudcore.webpanel.reports.view";

        // Reports Create Permissions
        /** Access to create reports */
        public static final String PANEL_REPORTS_CREATE = "cloudcore.webpanel.reports.create";

        // Reports Edit Permissions
        /** Access to edit reports */
        public static final String PANEL_REPORTS_EDIT = "cloudcore.webpanel.reports.edit";

        // Reports Delete Permissions
        /** Access to delete reports */
        public static final String PANEL_REPORTS_DELETE = "cloudcore.webpanel.reports.delete";

        // Reports Manage Permissions
        /** Access to manage reports */
        public static final String PANEL_REPORTS_MANAGE = "cloudcore.webpanel.reports.manage";

        // Staff List Index Permissions
        /** Access to be listed in staff list */
        public static final String PANEL_LIST_STAFF_INDEX = "cloudcore.webpanel.list.staff.index";

        // Workers Index Permissions
        /** Access to view workers */
        public static final String PANEL_WORKERS_INDEX = "cloudcore.webpanel.workers.index";

        // Workers Create Permissions
        /** Access to create workers */
        public static final String PANEL_WORKERS_CREATE = "cloudcore.webpanel.workers.create";

        // Workers Edit Permissions
        /** Access to edit workers */
        public static final String PANEL_WORKERS_EDIT = "cloudcore.webpanel.workers.edit";

        // Workers Delete Permissions
        /** Access to delete workers */
        public static final String PANEL_WORKERS_DELETE = "cloudcore.webpanel.workers.delete";

        // Workers Manage Permissions
        /** Access to manage workers */
        public static final String PANEL_WORKERS_MANAGE = "cloudcore.webpanel.workers.manage";

        // Console Tasks Index Permissions
        /** Access to view console tasks */
        public static final String PANEL_CONSOLE_TASKS_INDEX = "cloudcore.webpanel.console-tasks.index";

        // Console Tasks Create Permissions
        /** Access to create console tasks */
        public static final String PANEL_CONSOLE_TASKS_CREATE = "cloudcore.webpanel.console-tasks.create";

        // Console Tasks Edit Permissions
        /** Access to edit console tasks */
        public static final String PANEL_CONSOLE_TASKS_EDIT = "cloudcore.webpanel.console-tasks.edit";

        // Console Tasks Delete Permissions
        /** Access to delete console tasks */
        public static final String PANEL_CONSOLE_TASKS_DELETE = "cloudcore.webpanel.console-tasks.delete";

        // Console Tasks Manage Permissions
        /** Access to manage console tasks */
        public static final String PANEL_CONSOLE_TASKS_MANAGE = "cloudcore.webpanel.console-tasks.manage";

        // Store Categories Index Permissions
        /** Access to view store categories */
        public static final String PANEL_STORE_CATEGORIES_INDEX = "cloudcore.webpanel.store.categories.index";

        // Store Categories Create Permissions
        /** Access to create store categories */
        public static final String PANEL_STORE_CATEGORIES_CREATE = "cloudcore.webpanel.store.categories.create";

        // Store Categories Edit Permissions
        /** Access to edit store categories */
        public static final String PANEL_STORE_CATEGORIES_EDIT = "cloudcore.webpanel.store.categories.edit";

        // Store Categories Delete Permissions
        /** Access to delete store categories */
        public static final String PANEL_STORE_CATEGORIES_DELETE = "cloudcore.webpanel.store.categories.delete";

        // Store Categories Manage Permissions
        /** Access to manage store categories */
        public static final String PANEL_STORE_CATEGORIES_MANAGE = "cloudcore.webpanel.store.categories.manage";

        // Store Packages Index Permissions
        /** Access to view store packages */
        public static final String PANEL_STORE_PACKAGES_INDEX = "cloudcore.webpanel.store.packages.index";

        // Store Packages Create Permissions
        /** Access to create store packages */
        public static final String PANEL_STORE_PACKAGES_CREATE = "cloudcore.webpanel.store.packages.create";

        // Store Packages Edit Permissions
        /** Access to edit store packages */
        public static final String PANEL_STORE_PACKAGES_EDIT = "cloudcore.webpanel.store.packages.edit";

        // Store Packages Delete Permissions
        /** Access to delete store packages */
        public static final String PANEL_STORE_PACKAGES_DELETE = "cloudcore.webpanel.store.packages.delete";

        // Store Packages Manage Permissions
        /** Access to manage store packages */
        public static final String PANEL_STORE_PACKAGES_MANAGE = "cloudcore.webpanel.store.packages.manage";

        // Store Commands Index Permissions
        /** Access to view store commands */
        public static final String PANEL_STORE_COMMANDS_INDEX = "cloudcore.webpanel.store.commands.index";

        // Store Commands Create Permissions
        /** Access to create store commands */
        public static final String PANEL_STORE_COMMANDS_CREATE = "cloudcore.webpanel.store.commands.create";

        // Store Commands Edit Permissions
        /** Access to edit store commands */
        public static final String PANEL_STORE_COMMANDS_EDIT = "cloudcore.webpanel.store.commands.edit";

        // Store Commands Delete Permissions
        /** Access to delete store commands */
        public static final String PANEL_STORE_COMMANDS_DELETE = "cloudcore.webpanel.store.commands.delete";

        // Store Commands Manage Permissions
        /** Access to manage store commands */
        public static final String PANEL_STORE_COMMANDS_MANAGE = "cloudcore.webpanel.store.commands.manage";

        // Store Variables Index Permissions
        /** Access to view store variables */
        public static final String PANEL_STORE_VARIABLES_INDEX = "cloudcore.webpanel.store.variables.index";

        // Store Variables Create Permissions
        /** Access to create store variables */
        public static final String PANEL_STORE_VARIABLES_CREATE = "cloudcore.webpanel.store.variables.create";

        // Store Variables Edit Permissions
        /** Access to edit store variables */
        public static final String PANEL_STORE_VARIABLES_EDIT = "cloudcore.webpanel.store.variables.edit";

        // Store Variables Delete Permissions
        /** Access to delete store variables */
        public static final String PANEL_STORE_VARIABLES_DELETE = "cloudcore.webpanel.store.variables.delete";

        // Store Variables Manage Permissions
        /** Access to manage store variables */
        public static final String PANEL_STORE_VARIABLES_MANAGE = "cloudcore.webpanel.store.variables.manage";

        // Messages Index Permissions
        /** Access to view messages */
        public static final String PANEL_MESSAGES_INDEX = "cloudcore.webpanel.messages.index";

        // Messages Create Permissions
        /** Access to create messages */
        public static final String PANEL_MESSAGES_CREATE = "cloudcore.webpanel.messages.create";

        // Messages Edit Permissions
        /** Access to edit messages */
        public static final String PANEL_MESSAGES_EDIT = "cloudcore.webpanel.messages.edit";

        // Messages Delete Permissions
        /** Access to delete messages */
        public static final String PANEL_MESSAGES_DELETE = "cloudcore.webpanel.messages.delete";

        // Messages Manage Permissions
        /** Access to manage messages */
        public static final String PANEL_MESSAGES_MANAGE = "cloudcore.webpanel.messages.manage";

        // Maintenance Index Permissions
        /** Access to view maintenance settings */
        public static final String PANEL_MAINTENANCE_INDEX = "cloudcore.webpanel.maintenance.index";

        // Maintenance Create Permissions
        /** Access to create maintenance schedules */
        public static final String PANEL_MAINTENANCE_CREATE = "cloudcore.webpanel.maintenance.create";

        // Console Executors Index Permissions
        /** Access to view console executors */
        public static final String PANEL_CONSOLE_EXECUTORS_INDEX = "cloudcore.webpanel.console-executors.index";

        // Invoices Index Permissions
        /** Access to view invoices */
        public static final String PANEL_INVOICES_INDEX = "cloudcore.webpanel.invoices.index";

        // Invoices Create Permissions
        /** Access to create invoices */
        public static final String PANEL_INVOICES_CREATE = "cloudcore.webpanel.invoices.create";

        // Private constructor to prevent instantiation
        private Permissions() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        /**
         * Returns all permission nodes with metadata.
         * 
         * @return java.util.Map<String, PermissionInfo>
         */
        public static java.util.Map<String, PermissionInfo> getAllPermissions() {
            java.util.Map<String, PermissionInfo> permissions = new java.util.HashMap<>();
            permissions.put(VPN_BYPASS,
                    new PermissionInfo(VPN_BYPASS, "VPN Bypass", "Allows bypassing VPN restrictions"));
            permissions.put(ALTS_BYPASS,
                    new PermissionInfo(ALTS_BYPASS, "Alts Bypass", "Allows bypassing alt account restrictions"));
            permissions.put(CHAT_BYPASS,
                    new PermissionInfo(CHAT_BYPASS, "Chat Bypass", "Allows bypassing chat restrictions"));
            permissions.put(CHAT_COLOR, new PermissionInfo(CHAT_COLOR, "Chat Color", "Allows using color in chat"));
            permissions.put(COMMAND_BYPASS,
                    new PermissionInfo(COMMAND_BYPASS, "Command Bypass", "Allows bypassing command restrictions"));
            permissions.put(CONSOLE_EXECUTE,
                    new PermissionInfo(CONSOLE_EXECUTE, "Console Execute", "Allows executing console commands"));
            permissions.put(REPORT_USE, new PermissionInfo(REPORT_USE, "Report Use", "Allows using the report system"));
            permissions.put(REPORT_NOTIFY,
                    new PermissionInfo(REPORT_NOTIFY, "Report Notify", "Receives notifications about reports"));
            permissions.put(ALERT_USE, new PermissionInfo(ALERT_USE, "Alert Use", "Allows using the alert system"));
            permissions.put(MAINTENANCE_BYPASS,
                    new PermissionInfo(MAINTENANCE_BYPASS, "Maintenance Bypass", "Allows access during maintenance"));
            permissions.put(MAINTENANCE_TOGGLE,
                    new PermissionInfo(MAINTENANCE_TOGGLE, "Maintenance Toggle", "Allows toggling maintenance mode"));
            permissions.put(MAINTENANCE_MANAGE, new PermissionInfo(MAINTENANCE_MANAGE, "Maintenance Manage",
                    "Allows managing maintenance settings"));
            permissions.put(ADMIN_RELOAD,
                    new PermissionInfo(ADMIN_RELOAD, "Admin Reload", "Allows reloading the system"));
            permissions.put(ADMIN_CLEAR_CACHE,
                    new PermissionInfo(ADMIN_CLEAR_CACHE, "Admin Clear Cache", "Allows clearing system cache"));
            permissions.put(ADMIN_VERSION,
                    new PermissionInfo(ADMIN_VERSION, "Admin Version", "Allows checking system version"));
            permissions.put(ADMIN_INFO,
                    new PermissionInfo(ADMIN_INFO, "Admin Info", "Allows viewing system information"));
            permissions.put(SETTINGS_MANAGE,
                    new PermissionInfo(SETTINGS_MANAGE, "Settings Manage", "Allows managing system settings"));
            permissions.put(CHATLOG_COMMAND,
                    new PermissionInfo(CHATLOG_COMMAND, "Chatlog Command", "Allows accessing chat logs"));
            
            permissions.put(PANEL_ADMIN,
                    new PermissionInfo(PANEL_ADMIN, "Panel Admin", "Full administrative access to the web panel"));
            permissions.put(PANEL_USERS_INDEX,
                    new PermissionInfo(PANEL_USERS_INDEX, "Users Index", "Access to view user list"));
            permissions.put(PANEL_USERS_EDIT,
                    new PermissionInfo(PANEL_USERS_EDIT, "Users Edit", "Access to edit users"));
            permissions.put(PANEL_USERS_DELETE,
                    new PermissionInfo(PANEL_USERS_DELETE, "Users Delete", "Access to delete users"));
            permissions.put(PANEL_TICKETS_INDEX,
                    new PermissionInfo(PANEL_TICKETS_INDEX, "Tickets Index", "Access to view tickets"));
            permissions.put(PANEL_TICKETS_DETAILS,
                    new PermissionInfo(PANEL_TICKETS_DETAILS, "Tickets Details", "Access to view ticket details"));
            permissions.put(PANEL_SETTINGS_INDEX,
                    new PermissionInfo(PANEL_SETTINGS_INDEX, "Settings Index", "Access to view settings"));
            permissions.put(PANEL_SETTINGS_EDIT,
                    new PermissionInfo(PANEL_SETTINGS_EDIT, "Settings Edit", "Access to edit settings"));
            permissions.put(PANEL_SETTINGS_DELETE,
                    new PermissionInfo(PANEL_SETTINGS_DELETE, "Settings Delete", "Access to delete settings"));
            permissions.put(PANEL_REDIRECT_LINKS_INDEX, new PermissionInfo(PANEL_REDIRECT_LINKS_INDEX,
                    "Redirect Links Index", "Access to view redirect links"));
            permissions.put(PANEL_REDIRECT_LINKS_CREATE, new PermissionInfo(PANEL_REDIRECT_LINKS_CREATE,
                    "Redirect Links Create", "Access to create redirect links"));
            permissions.put(PANEL_REDIRECT_LINKS_EDIT, new PermissionInfo(PANEL_REDIRECT_LINKS_EDIT,
                    "Redirect Links Edit", "Access to edit redirect links"));
            permissions.put(PANEL_REDIRECT_LINKS_DELETE, new PermissionInfo(PANEL_REDIRECT_LINKS_DELETE,
                    "Redirect Links Delete", "Access to delete redirect links"));
            permissions.put(PANEL_REDEEM_CODES_INDEX,
                    new PermissionInfo(PANEL_REDEEM_CODES_INDEX, "Redeem Codes Index", "Access to view redeem codes"));
            permissions.put(PANEL_REDEEM_CODES_CREATE, new PermissionInfo(PANEL_REDEEM_CODES_CREATE,
                    "Redeem Codes Create", "Access to create redeem codes"));
            permissions.put(PANEL_REDEEM_CODES_EDIT,
                    new PermissionInfo(PANEL_REDEEM_CODES_EDIT, "Redeem Codes Edit", "Access to edit redeem codes"));
            permissions.put(PANEL_REDEEM_CODES_DELETE, new PermissionInfo(PANEL_REDEEM_CODES_DELETE,
                    "Redeem Codes Delete", "Access to delete redeem codes"));
            permissions.put(PANEL_PLUGINS_INDEX,
                    new PermissionInfo(PANEL_PLUGINS_INDEX, "Plugins Index", "Access to view plugins"));
            permissions.put(PANEL_PLUGINS_CONFIG,
                    new PermissionInfo(PANEL_PLUGINS_CONFIG, "Plugins Config", "Access to configure plugins"));
            permissions.put(PANEL_MAIL_TEMPLATES_INDEX, new PermissionInfo(PANEL_MAIL_TEMPLATES_INDEX,
                    "Mail Templates Index", "Access to view mail templates"));
            permissions.put(PANEL_MAIL_TEMPLATES_CREATE, new PermissionInfo(PANEL_MAIL_TEMPLATES_CREATE,
                    "Mail Templates Create", "Access to create mail templates"));
            permissions.put(PANEL_MAIL_TEMPLATES_EDIT, new PermissionInfo(PANEL_MAIL_TEMPLATES_EDIT,
                    "Mail Templates Edit", "Access to edit mail templates"));
            permissions.put(PANEL_MAIL_TEMPLATES_DELETE, new PermissionInfo(PANEL_MAIL_TEMPLATES_DELETE,
                    "Mail Templates Delete", "Access to delete mail templates"));
            permissions.put(PANEL_ANNOUNCEMENTS_INDEX, new PermissionInfo(PANEL_ANNOUNCEMENTS_INDEX,
                    "Announcements Index", "Access to view announcements"));
            permissions.put(PANEL_ANNOUNCEMENTS_CREATE, new PermissionInfo(PANEL_ANNOUNCEMENTS_CREATE,
                    "Announcements Create", "Access to create announcements"));
            permissions.put(PANEL_ANNOUNCEMENTS_EDIT,
                    new PermissionInfo(PANEL_ANNOUNCEMENTS_EDIT, "Announcements Edit", "Access to edit announcements"));
            permissions.put(PANEL_ANNOUNCEMENTS_DELETE, new PermissionInfo(PANEL_ANNOUNCEMENTS_DELETE,
                    "Announcements Delete", "Access to delete announcements"));
            permissions.put(PANEL_BACKUPS_INDEX,
                    new PermissionInfo(PANEL_BACKUPS_INDEX, "Backups Index", "Access to view backups"));
            permissions.put(PANEL_DEPARTMENTS_INDEX,
                    new PermissionInfo(PANEL_DEPARTMENTS_INDEX, "Departments Index", "Access to view departments"));
            permissions.put(PANEL_DEPARTMENTS_CREATE,
                    new PermissionInfo(PANEL_DEPARTMENTS_CREATE, "Departments Create", "Access to create departments"));
            permissions.put(PANEL_DEPARTMENTS_EDIT,
                    new PermissionInfo(PANEL_DEPARTMENTS_EDIT, "Departments Edit", "Access to edit departments"));
            permissions.put(PANEL_DEPARTMENTS_DELETE,
                    new PermissionInfo(PANEL_DEPARTMENTS_DELETE, "Departments Delete", "Access to delete departments"));
            permissions.put(PANEL_IMAGES_INDEX,
                    new PermissionInfo(PANEL_IMAGES_INDEX, "Images Index", "Access to view images"));
            permissions.put(PANEL_IMAGES_CREATE,
                    new PermissionInfo(PANEL_IMAGES_CREATE, "Images Create", "Access to create images"));
            permissions.put(PANEL_IMAGES_DELETE,
                    new PermissionInfo(PANEL_IMAGES_DELETE, "Images Delete", "Access to delete images"));
            permissions.put(PANEL_RULES_INDEX,
                    new PermissionInfo(PANEL_RULES_INDEX, "Rules Index", "Access to view rules"));
            permissions.put(PANEL_RULES_CREATE,
                    new PermissionInfo(PANEL_RULES_CREATE, "Rules Create", "Access to create rules"));
            permissions.put(PANEL_RULES_EDIT,
                    new PermissionInfo(PANEL_RULES_EDIT, "Rules Edit", "Access to edit rules"));
            permissions.put(PANEL_RULES_DELETE,
                    new PermissionInfo(PANEL_RULES_DELETE, "Rules Delete", "Access to delete rules"));
            permissions.put(PANEL_PAGES_INDEX,
                    new PermissionInfo(PANEL_PAGES_INDEX, "Pages Index", "Access to view pages"));
            permissions.put(PANEL_WIDGET_OVERVIEW,
                    new PermissionInfo(PANEL_WIDGET_OVERVIEW, "Widget Overview", "Access to overview widget"));
            permissions.put(PANEL_WIDGET_AT_A_GLANCE,
                    new PermissionInfo(PANEL_WIDGET_AT_A_GLANCE, "Widget At a Glance", "Access to at-a-glance widget"));
            permissions.put(PANEL_WIDGET_ACTIVITY,
                    new PermissionInfo(PANEL_WIDGET_ACTIVITY, "Widget Activity", "Access to activity widget"));
            permissions.put(PANEL_WIDGET_QUICK_ACTIONS, new PermissionInfo(PANEL_WIDGET_QUICK_ACTIONS,
                    "Widget Quick Actions", "Access to quick actions widget"));
            permissions.put(PANEL_WIDGET_SUPPORT,
                    new PermissionInfo(PANEL_WIDGET_SUPPORT, "Widget Support", "Access to support widget"));
            permissions.put(PANEL_WIDGET_LOGS,
                    new PermissionInfo(PANEL_WIDGET_LOGS, "Widget Logs", "Access to logs widget"));
            permissions.put(PANEL_APPLICATIONS_INDEX,
                    new PermissionInfo(PANEL_APPLICATIONS_INDEX, "Applications Index", "Access to view applications"));
            permissions.put(PANEL_APPLICATIONS_VIEW, new PermissionInfo(PANEL_APPLICATIONS_VIEW, "Applications View",
                    "Access to view application details"));
            permissions.put(PANEL_APPLICATIONS_REVIEW, new PermissionInfo(PANEL_APPLICATIONS_REVIEW,
                    "Applications Review", "Access to review applications"));
            permissions.put(PANEL_APPLICATIONS_DELETE, new PermissionInfo(PANEL_APPLICATIONS_DELETE,
                    "Applications Delete", "Access to delete applications"));
            permissions.put(PANEL_APPLICATIONS_POSITIONS_MANAGE, new PermissionInfo(PANEL_APPLICATIONS_POSITIONS_MANAGE,
                    "Applications Positions Manage", "Access to manage application positions"));
            permissions.put(PANEL_APPLICATIONS_QUESTIONS_MANAGE, new PermissionInfo(PANEL_APPLICATIONS_QUESTIONS_MANAGE,
                    "Applications Questions Manage", "Access to manage application questions"));
            permissions.put(PANEL_REPORTS_INDEX,
                    new PermissionInfo(PANEL_REPORTS_INDEX, "Reports Index", "Access to view reports"));
            permissions.put(PANEL_REPORTS_VIEW,
                    new PermissionInfo(PANEL_REPORTS_VIEW, "Reports View", "Access to view report details"));
            permissions.put(PANEL_REPORTS_CREATE,
                    new PermissionInfo(PANEL_REPORTS_CREATE, "Reports Create", "Access to create reports"));
            permissions.put(PANEL_REPORTS_EDIT,
                    new PermissionInfo(PANEL_REPORTS_EDIT, "Reports Edit", "Access to edit reports"));
            permissions.put(PANEL_REPORTS_DELETE,
                    new PermissionInfo(PANEL_REPORTS_DELETE, "Reports Delete", "Access to delete reports"));
            permissions.put(PANEL_REPORTS_MANAGE,
                    new PermissionInfo(PANEL_REPORTS_MANAGE, "Reports Manage", "Access to manage reports"));
            permissions.put(PANEL_LIST_STAFF_INDEX, new PermissionInfo(PANEL_LIST_STAFF_INDEX, "Staff List Index",
                    "Access to be listed in staff list"));
            permissions.put(PANEL_WORKERS_INDEX,
                    new PermissionInfo(PANEL_WORKERS_INDEX, "Workers Index", "Access to view workers"));
            permissions.put(PANEL_WORKERS_CREATE,
                    new PermissionInfo(PANEL_WORKERS_CREATE, "Workers Create", "Access to create workers"));
            permissions.put(PANEL_WORKERS_EDIT,
                    new PermissionInfo(PANEL_WORKERS_EDIT, "Workers Edit", "Access to edit workers"));
            permissions.put(PANEL_WORKERS_DELETE,
                    new PermissionInfo(PANEL_WORKERS_DELETE, "Workers Delete", "Access to delete workers"));
            permissions.put(PANEL_WORKERS_MANAGE,
                    new PermissionInfo(PANEL_WORKERS_MANAGE, "Workers Manage", "Access to manage workers"));
            permissions.put(PANEL_CONSOLE_TASKS_INDEX, new PermissionInfo(PANEL_CONSOLE_TASKS_INDEX,
                    "Console Tasks Index", "Access to view console tasks"));
            permissions.put(PANEL_CONSOLE_TASKS_CREATE, new PermissionInfo(PANEL_CONSOLE_TASKS_CREATE,
                    "Console Tasks Create", "Access to create console tasks"));
            permissions.put(PANEL_CONSOLE_TASKS_EDIT,
                    new PermissionInfo(PANEL_CONSOLE_TASKS_EDIT, "Console Tasks Edit", "Access to edit console tasks"));
            permissions.put(PANEL_CONSOLE_TASKS_DELETE, new PermissionInfo(PANEL_CONSOLE_TASKS_DELETE,
                    "Console Tasks Delete", "Access to delete console tasks"));
            permissions.put(PANEL_CONSOLE_TASKS_MANAGE, new PermissionInfo(PANEL_CONSOLE_TASKS_MANAGE,
                    "Console Tasks Manage", "Access to manage console tasks"));
            permissions.put(PANEL_STORE_CATEGORIES_INDEX, new PermissionInfo(PANEL_STORE_CATEGORIES_INDEX,
                    "Store Categories Index", "Access to view store categories"));
            permissions.put(PANEL_STORE_CATEGORIES_CREATE, new PermissionInfo(PANEL_STORE_CATEGORIES_CREATE,
                    "Store Categories Create", "Access to create store categories"));
            permissions.put(PANEL_STORE_CATEGORIES_EDIT, new PermissionInfo(PANEL_STORE_CATEGORIES_EDIT,
                    "Store Categories Edit", "Access to edit store categories"));
            permissions.put(PANEL_STORE_CATEGORIES_DELETE, new PermissionInfo(PANEL_STORE_CATEGORIES_DELETE,
                    "Store Categories Delete", "Access to delete store categories"));
            permissions.put(PANEL_STORE_CATEGORIES_MANAGE, new PermissionInfo(PANEL_STORE_CATEGORIES_MANAGE,
                    "Store Categories Manage", "Access to manage store categories"));
            permissions.put(PANEL_STORE_PACKAGES_INDEX, new PermissionInfo(PANEL_STORE_PACKAGES_INDEX,
                    "Store Packages Index", "Access to view store packages"));
            permissions.put(PANEL_STORE_PACKAGES_CREATE, new PermissionInfo(PANEL_STORE_PACKAGES_CREATE,
                    "Store Packages Create", "Access to create store packages"));
            permissions.put(PANEL_STORE_PACKAGES_EDIT, new PermissionInfo(PANEL_STORE_PACKAGES_EDIT,
                    "Store Packages Edit", "Access to edit store packages"));
            permissions.put(PANEL_STORE_PACKAGES_DELETE, new PermissionInfo(PANEL_STORE_PACKAGES_DELETE,
                    "Store Packages Delete", "Access to delete store packages"));
            permissions.put(PANEL_STORE_PACKAGES_MANAGE, new PermissionInfo(PANEL_STORE_PACKAGES_MANAGE,
                    "Store Packages Manage", "Access to manage store packages"));
            permissions.put(PANEL_STORE_COMMANDS_INDEX, new PermissionInfo(PANEL_STORE_COMMANDS_INDEX,
                    "Store Commands Index", "Access to view store commands"));
            permissions.put(PANEL_STORE_COMMANDS_CREATE, new PermissionInfo(PANEL_STORE_COMMANDS_CREATE,
                    "Store Commands Create", "Access to create store commands"));
            permissions.put(PANEL_STORE_COMMANDS_EDIT, new PermissionInfo(PANEL_STORE_COMMANDS_EDIT,
                    "Store Commands Edit", "Access to edit store commands"));
            permissions.put(PANEL_STORE_COMMANDS_DELETE, new PermissionInfo(PANEL_STORE_COMMANDS_DELETE,
                    "Store Commands Delete", "Access to delete store commands"));
            permissions.put(PANEL_STORE_COMMANDS_MANAGE, new PermissionInfo(PANEL_STORE_COMMANDS_MANAGE,
                    "Store Commands Manage", "Access to manage store commands"));
            permissions.put(PANEL_STORE_VARIABLES_INDEX, new PermissionInfo(PANEL_STORE_VARIABLES_INDEX,
                    "Store Variables Index", "Access to view store variables"));
            permissions.put(PANEL_STORE_VARIABLES_CREATE, new PermissionInfo(PANEL_STORE_VARIABLES_CREATE,
                    "Store Variables Create", "Access to create store variables"));
            permissions.put(PANEL_STORE_VARIABLES_EDIT, new PermissionInfo(PANEL_STORE_VARIABLES_EDIT,
                    "Store Variables Edit", "Access to edit store variables"));
            permissions.put(PANEL_STORE_VARIABLES_DELETE, new PermissionInfo(PANEL_STORE_VARIABLES_DELETE,
                    "Store Variables Delete", "Access to delete store variables"));
            permissions.put(PANEL_STORE_VARIABLES_MANAGE, new PermissionInfo(PANEL_STORE_VARIABLES_MANAGE,
                    "Store Variables Manage", "Access to manage store variables"));
            permissions.put(PANEL_MESSAGES_INDEX,
                    new PermissionInfo(PANEL_MESSAGES_INDEX, "Messages Index", "Access to view messages"));
            permissions.put(PANEL_MESSAGES_CREATE,
                    new PermissionInfo(PANEL_MESSAGES_CREATE, "Messages Create", "Access to create messages"));
            permissions.put(PANEL_MESSAGES_EDIT,
                    new PermissionInfo(PANEL_MESSAGES_EDIT, "Messages Edit", "Access to edit messages"));
            permissions.put(PANEL_MESSAGES_DELETE,
                    new PermissionInfo(PANEL_MESSAGES_DELETE, "Messages Delete", "Access to delete messages"));
            permissions.put(PANEL_MESSAGES_MANAGE,
                    new PermissionInfo(PANEL_MESSAGES_MANAGE, "Messages Manage", "Access to manage messages"));
            permissions.put(PANEL_MAINTENANCE_INDEX, new PermissionInfo(PANEL_MAINTENANCE_INDEX, "Maintenance Index",
                    "Access to view maintenance settings"));
            permissions.put(PANEL_MAINTENANCE_CREATE, new PermissionInfo(PANEL_MAINTENANCE_CREATE, "Maintenance Create",
                    "Access to create maintenance schedules"));
            permissions.put(PANEL_CONSOLE_EXECUTORS_INDEX, new PermissionInfo(PANEL_CONSOLE_EXECUTORS_INDEX,
                    "Console Executors Index", "Access to view console executors"));
            permissions.put(PANEL_INVOICES_INDEX,
                    new PermissionInfo(PANEL_INVOICES_INDEX, "Invoices Index", "Access to view invoices"));
            permissions.put(PANEL_INVOICES_CREATE,
                    new PermissionInfo(PANEL_INVOICES_CREATE, "Invoices Create", "Access to create invoices"));
            return permissions;
        }

        /**
         * Inner class to hold permission metadata
         */
        public static class PermissionInfo {
            private final String value;
            private final String category;
            private final String description;

            public PermissionInfo(String value, String category, String description) {
                this.value = value;
                this.category = category;
                this.description = description;
            }

            public String getValue() {
                return value;
            }

            public String getCategory() {
                return category;
            }

            public String getDescription() {
                return description;
            }
        }
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
        public static final String GLOBAL_STORE_URL = "store_url";
        public static final String GLOBAL_ALLOW_CODE_REDEEM = "code_redemption_enabled";

        

        public static List<String> getGlobalSettings() {
            return Arrays.asList(
                    FIREWALL_ENABLED,
                    FIREWALL_BLOCK_VPN,
                    FIREWALL_BLOCK_ALTS,
                    LOG_CHAT,
                    LOG_COMMANDS,
                    REPORT_SYSTEM_ENABLED,
                    REPORT_COOLDOWN,
                    MAINTENANCE_MODE,
                    ENABLE_ALERT_COMMAND,
                    ENABLE_CONSOLE_COMMAND,
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
                    GLOBAL_STORE_URL,
                    GLOBAL_ALLOW_CODE_REDEEM);
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

        // Social Media Messages
        public static final String SOCIAL_CLICK_TO_VIEW = "social.click_to_view";
        public static final String SOCIAL_LINK_FORMAT = "social.link_format";
        public static final String SOCIAL_HOVER_TEXT = "social.hover_text";

        

        // Chatlog messages
        public static final String CHATLOG_COOLDOWN = "chatlog.cooldown";
    }
}