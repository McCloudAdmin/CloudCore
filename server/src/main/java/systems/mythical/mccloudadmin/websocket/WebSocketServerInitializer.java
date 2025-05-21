package systems.mythical.mccloudadmin.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import systems.mythical.mccloudadmin.auth.AuthHandler;
import systems.mythical.mccloudadmin.config.ServerConfig;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final ServerConfig config = ServerConfig.getInstance();

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // HTTP request decoder and response encoder
        pipeline.addLast(new HttpServerCodec());
        
        // Handles chunked HTTP messages
        pipeline.addLast(new ChunkedWriteHandler());
        
        // Aggregates HTTP messages
        pipeline.addLast(new HttpObjectAggregator(config.getMaxFrameSize()));
        
        // Authentication handler
        pipeline.addLast(new AuthHandler());
        
        // WebSocket protocol handler
        pipeline.addLast(new WebSocketServerProtocolHandler(config.getWebSocketPath(), null, true));
        
        // Custom WebSocket handler
        pipeline.addLast(new WebSocketFrameHandler());
    }
} 