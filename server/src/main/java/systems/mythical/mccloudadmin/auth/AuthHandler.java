package systems.mythical.mccloudadmin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.ServerConfig;
import systems.mythical.mccloudadmin.model.WebSocketMessage;


public class AuthHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private static final ServerConfig config = ServerConfig.getInstance();
    private static final String key = config.getJwtSecret();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String token = request.headers().get(HttpHeaderNames.AUTHORIZATION);

            if (token == null) {
                logger.warn("Authentication failed: No token provided from {}", ctx.channel().remoteAddress());
                sendAuthError(ctx, "Authentication required. Please provide a valid JWT token.");
                return;
            }

            if (!token.startsWith("Bearer ")) {
                logger.warn("Authentication failed: Invalid token format from {}", ctx.channel().remoteAddress());
                sendAuthError(ctx, "Invalid token format. Token must start with 'Bearer '");
                return;
            }

            token = token.substring(7);
            try {
                if (token.equals(key)) {
                    ctx.fireChannelRead(msg);
                } else {
                    sendAuthError(ctx, "Invalid token. Please provide a valid token.");
                }
            } catch (Exception e) {
                logger.warn("Authentication failed: Malformed token from {}", ctx.channel().remoteAddress());
                sendAuthError(ctx, "Invalid token format. Please provide a valid token.");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void sendAuthError(ChannelHandlerContext ctx, String message) {
        try {
            WebSocketMessage error = new WebSocketMessage("auth_error", message);
            String jsonError = objectMapper.writeValueAsString(error);
            ctx.writeAndFlush(new TextWebSocketFrame(jsonError))
                    .addListener(future -> ctx.close());
        } catch (Exception e) {
            logger.error("Error sending authentication error message", e);
            ctx.close();
        }
    }
}