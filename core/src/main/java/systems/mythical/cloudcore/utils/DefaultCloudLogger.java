package systems.mythical.cloudcore.utils;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultCloudLogger implements CloudLogger {
    private final Logger backend;
    private Supplier<Boolean> debugSupplier = () -> false;

    public DefaultCloudLogger(Logger backend) {
        this.backend = Objects.requireNonNull(backend);
    }

    @Override
    public void info(String message) {
        backend.log(Level.INFO, message);
    }

    @Override
    public void warn(String message) {
        backend.log(Level.WARNING, message);
    }

    @Override
    public void error(String message) {
        backend.log(Level.SEVERE, message);
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            backend.log(Level.INFO, message);
        } else {
            backend.log(Level.FINE, message);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return debugSupplier.get();
    }

    @Override
    public void setDebugSupplier(Supplier<Boolean> debugSupplier) {
        this.debugSupplier = debugSupplier != null ? debugSupplier : () -> false;
    }
}


