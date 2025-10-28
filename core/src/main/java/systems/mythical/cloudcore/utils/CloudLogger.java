package systems.mythical.cloudcore.utils;

import java.util.function.Supplier;

public interface CloudLogger {
    void info(String message);
    void warn(String message);
    void error(String message);
    void debug(String message);
    boolean isDebugEnabled();
    void setDebugSupplier(Supplier<Boolean> debugSupplier);
}


