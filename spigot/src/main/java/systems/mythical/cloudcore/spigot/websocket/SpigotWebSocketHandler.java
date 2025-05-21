package systems.mythical.cloudcore.spigot.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;

public class SpigotWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final WebSocketClientHandshaker handshaker;
    private final SpigotWebSocketClient client;

    public SpigotWebSocketHandler(WebSocketClientHandshaker handshaker, SpigotWebSocketClient client) {
        this.handshaker = handshaker;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        client.setConnected(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String message = frame.text();
        // TODO: Handle incoming messages
        client.getPlugin().getLogger().info("Received message: " + message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof WebSocketHandshakeException) {
            client.getPlugin().getLogger().severe("WebSocket handshake failed: " + cause.getMessage());
        } else {
            client.getPlugin().getLogger().severe("WebSocket error: " + cause.getMessage());
        }
        ctx.close();
    }
} 