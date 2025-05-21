package systems.mythical.cloudcore.spigot.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.bukkit.Server;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

import java.net.URI;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class SpigotWebSocketClient {
    private final CloudCoreSpigot plugin;
    private final Server server;
    private final EventLoopGroup group;
    private Channel channel;
    private WebSocketClientHandshaker handshaker;
    private int reconnectAttempts = 0;
    private boolean isConnected = false;
    private boolean isShuttingDown = false;

    public SpigotWebSocketClient(CloudCoreSpigot plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
        this.group = new NioEventLoopGroup();
    }

    public void connect() {
        try {
            String url = String.format("ws://%s:%d%s", 
                plugin.getPluginConfig().getServerHost(), 
                plugin.getPluginConfig().getServerPort(), 
                plugin.getPluginConfig().getWebSocketPath());
            
            URI uri = new URI(url);
            handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, 
                new DefaultHttpHeaders().add("Authorization", "Bearer " + plugin.getPluginConfig().get("server.password")),
                plugin.getPluginConfig().getConnectionTimeout()
            );

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, plugin.getPluginConfig().getConnectionTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (p.get("ws-encoder") != null) {
                            p.remove("ws-encoder");
                        }
                        if (p.get("ws-decoder") != null) {
                            p.remove("ws-decoder");
                        }
                        p.addLast(
                            new LoggingHandler(LogLevel.INFO),
                            new ReadTimeoutHandler(plugin.getPluginConfig().getConnectionTimeout(), TimeUnit.MILLISECONDS),
                            new HttpClientCodec(),
                            new HttpObjectAggregator(65536),
                            new SpigotWebSocketHandler(handshaker, SpigotWebSocketClient.this)
                        );
                    }
                });

            ChannelFuture future = bootstrap.connect(uri.getHost(), uri.getPort());
            future.addListener((ChannelFutureListener) f -> {
                if (!f.isSuccess()) {
                    plugin.getLogger().severe("Failed to connect to WebSocket server at " + url + ": " + f.cause().getMessage());
                    handleConnectionFailure();
                }
            });

            channel = future.sync().channel();
            handshaker.handshake(channel).sync();
            isConnected = true;
            reconnectAttempts = 0;
            plugin.getLogger().info("Successfully connected to WebSocket server at " + url);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to WebSocket server: " + e.getMessage());
            handleConnectionFailure();
        }
    }

    private void handleConnectionFailure() {
        if (reconnectAttempts < plugin.getPluginConfig().getMaxReconnectAttempts()) {
            reconnectAttempts++;
            int delay = Math.min(plugin.getPluginConfig().getReconnectInterval() * reconnectAttempts, 30000);
            plugin.getLogger().info("Attempting to reconnect in " + (delay/1000) + " seconds... (Attempt " + reconnectAttempts + "/" + plugin.getPluginConfig().getMaxReconnectAttempts() + ")");
            
            try {
                group.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException e) {
                plugin.getLogger().severe("Failed to schedule reconnection: " + e.getMessage());
                isShuttingDown = true;
                disconnect();
            }
        } else {
            plugin.getLogger().severe("Max reconnection attempts reached. Shutting down server...");
            isShuttingDown = true;
            disconnect();
            server.shutdown();
        }
    }

    public void disconnect() {
        if (channel != null) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                plugin.getLogger().warning("Interrupted while closing channel: " + e.getMessage());
            }
        }
        try {
            group.shutdownGracefully(0, 5000, TimeUnit.MILLISECONDS).sync();
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Interrupted while shutting down event loop: " + e.getMessage());
        }
        isConnected = false;
    }

    public void sendMessage(String message) {
        if (isConnected && channel != null) {
            channel.writeAndFlush(message);
        } else {
            plugin.getLogger().warning("Cannot send message: Not connected to WebSocket server");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
        if (!connected && !isShuttingDown) {
            handleConnectionFailure();
        }
    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }

    public CloudCoreSpigot getPlugin() {
        return plugin;
    }
} 