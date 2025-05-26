package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;

import java.util.List;

public class ProxyConsoleCommand implements SimpleCommand {
    private final CloudCoreVelocity plugin;
    private final ProxyServer server;
    private final LegacyComponentSerializer legacySerializer;

    public ProxyConsoleCommand(CloudCoreVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("cloudadmin.proxyconsole")) {
            source.sendMessage(legacySerializer.deserialize("&cYou do not have permission to use this command."));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(legacySerializer.deserialize("&cUsage: /proxyconsole <command>"));
            return;
        }

        String command = String.join(" ", args);
        server.getCommandManager().executeAsync(source, command);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("cloudadmin.proxyconsole");
    }
} 