package systems.mythical.cloudcore.settings;

public interface Setting<T> {
    String getName();
    T getDefaultValue();
    T parseValue(String value);
    String serializeValue(T value);
} 