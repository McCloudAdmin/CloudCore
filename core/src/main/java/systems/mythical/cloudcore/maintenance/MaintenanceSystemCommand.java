package systems.mythical.cloudcore.maintenance;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.util.UUID;
import java.util.logging.Logger;

public class MaintenanceSystemCommand {
    private static MaintenanceSystemManager maintenanceManager;

    public static void initialize(DatabaseManager databaseManager, Logger logger) {
        maintenanceManager = MaintenanceSystemManager.getInstance(databaseManager, logger);
    }

    public static boolean isInMaintenance(UUID uuid) {
        if (maintenanceManager == null) return false;
        return maintenanceManager.isInMaintenance(uuid);
    }

    public static boolean addMaintenance(UUID uuid) {
        if (maintenanceManager == null) return false;
        return maintenanceManager.addMaintenance(uuid);
    }

    public static boolean removeMaintenance(UUID uuid) {
        if (maintenanceManager == null) return false;
        return maintenanceManager.removeMaintenance(uuid);
    }

    public static boolean lockMaintenance(UUID uuid) {
        if (maintenanceManager == null) return false;
        return maintenanceManager.lockMaintenance(uuid);
    }

    public static boolean unlockMaintenance(UUID uuid) {
        if (maintenanceManager == null) return false;
        return maintenanceManager.unlockMaintenance(uuid);
    }
} 