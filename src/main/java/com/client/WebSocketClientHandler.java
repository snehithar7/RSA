package com.client;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 3/29/17.
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(WebSocketClientHandler.class.getName());
    private final WebSocketClientHandshaker handshaker;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Channel chan = ctx.getChannel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(chan, (HttpResponse) e.getMessage());
            logger.log(Level.FINE, "Client connected");
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame text = (TextWebSocketFrame) frame;
            logger.log(Level.FINE, "Received message: {0}", text.getText());
            // Decrypt and display message here
        } else if (frame instanceof PingWebSocketFrame) {
            logger.log(Level.FINER, "Received ping, sending pong");
            chan.write(new PongWebSocketFrame(frame.getBinaryData()));
        } else if (frame instanceof PongWebSocketFrame){
            logger.log(Level.FINER, "Received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            logger.log(Level.FINE, "Received close");
            chan.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        t.printStackTrace();
        e.getChannel().close();
    }

}
