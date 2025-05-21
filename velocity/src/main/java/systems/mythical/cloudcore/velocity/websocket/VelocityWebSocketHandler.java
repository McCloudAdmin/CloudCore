package systems.mythical.cloudcore.velocity.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;

public class VelocityWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final WebSocketClientHandshaker handshaker;
    private final VelocityWebSocketClient client;

    public VelocityWebSocketHandler(WebSocketClientHandshaker handshaker, VelocityWebSocketClient client) {
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
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        // Handle incoming messages
        String message = frame.text();
        // TODO: Implement message handling
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