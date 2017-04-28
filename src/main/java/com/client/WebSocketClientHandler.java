package com.client;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by alec on 3/29/17.
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
    private final WebSocketClientHandshaker handshaker;
    // This queue is the message queue for all message received from
    // the server.
    public Queue<String> msgqueue = new ConcurrentLinkedQueue<String>();

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    /**
     * Asynchronous message receiver
     *
     * @param ctx
     * @param e
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Channel chan = ctx.getChannel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(chan, (HttpResponse) e.getMessage());
            System.out.println("Handler: client connected");
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame text = (TextWebSocketFrame) frame;
            System.out.println("Handler: text message received: " + text.getText());
            msgqueue.add(text.getText());
        } else if (frame instanceof PingWebSocketFrame) {
            System.out.println("Handler: received ping, sending pong");
            chan.write(new PongWebSocketFrame(frame.getBinaryData()));
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("Handler: received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("Handler: received close");
            chan.close();
        }
    }

    /**
     * Exception handler
     *
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        t.printStackTrace();
        e.getChannel().close();
    }

}
