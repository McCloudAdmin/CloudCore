package systems.mythical.cloudcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import systems.mythical.cloudcore.core.CloudCore;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "cloudcore", name = "CloudCore", version = "1.0-SNAPSHOT", description = "A simple MySQL connection plugin", authors = {
        "MythicalSystems" })
public class CloudCoreVelocity {
    private CloudCore core;

    @Inject
    public CloudCoreVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        core = new CloudCore(dataFolder.toFile(), logger);
    }

    public void shutdown() {
        if (core != null) {
            core.shutdown();
        }
    }
}