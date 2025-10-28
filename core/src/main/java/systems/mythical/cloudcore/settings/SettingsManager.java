package systems.mythical.cloudcore.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class SettingsManager {
    private static SettingsManager instance;
    private final CloudSettings cloudSettings;
    private final Map<String, Setting<?>> registeredSettings;
    private final CloudLogger cloudLogger = CloudLoggerFactory.get();

    private SettingsManager(CloudSettings cloudSettings, Logger logger) {
        this.cloudSettings = cloudSettings;
        this.registeredSettings = new HashMap<>();
    }

    public static SettingsManager getInstance(CloudSettings cloudSettings, Logger logger) {
        if (instance == null) {
            instance = new SettingsManager(cloudSettings, logger);
        }
        return instance;
    }

    public <T> void registerSetting(Setting<T> setting) {
        registeredSettings.put(setting.getName(), setting);
    }

    public <T> T getValue(Setting<T> setting) {
        String rawValue = cloudSettings.getSetting(setting.getName());
        if (rawValue == null || rawValue.isEmpty()) {
            return setting.getDefaultValue();
        }
        try {
            return setting.parseValue(rawValue);
        } catch (Exception e) {
            cloudLogger.warn("Error parsing setting " + setting.getName() + ": " + e.getMessage());
            return setting.getDefaultValue();
        }
    }

    public <T> void setValue(Setting<T> setting, T value) {
        String serialized = setting.serializeValue(value);
        cloudSettings.setSetting(setting.getName(), serialized);
    }

    public <T> void resetToDefault(Setting<T> setting) {
        setValue(setting, setting.getDefaultValue());
    }
} 