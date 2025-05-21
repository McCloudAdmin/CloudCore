package systems.mythical.mccloudadmin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.DatabaseConfig;
import systems.mythical.mccloudadmin.config.ServerConfig;
import systems.mythical.mccloudadmin.websocket.WebSocketServerInitializer;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ServerConfig config = ServerConfig.getInstance();
    private static final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();

    public static void main(String[] args) throws Exception {
        // Initialize database connection
        if (!databaseConfig.testConnection()) {
            logger.error("Failed to connect to database. Exiting...");
            System.exit(1);
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel ch = b.bind(config.getHost(), config.getPort()).sync().channel();
            logger.info("WebSocket server started on {}:{}", config.getHost(), config.getPort());
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            databaseConfig.close();
        }
    }
} 