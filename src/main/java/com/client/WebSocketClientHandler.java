package com.client;

import com.Ostermiller.util.CircularByteBuffer;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 3/29/17.
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler
{
    private final WebSocketClientHandshaker handshaker;
//    private CircularByteBuffer cbb;
//    private PrintWriter writer;
//    private InputStreamReader messageReader;
//    public BufferedReader reader;
    public Queue<String> msgqueue = new ConcurrentLinkedQueue<String>();

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
        // Initialize streams
//        cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
//        writer = new PrintWriter(cbb.getOutputStream());
//        messageReader = new InputStreamReader(cbb.getInputStream());
//        reader = new BufferedReader(messageReader);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    {
        Channel chan = ctx.getChannel();

        if (!handshaker.isHandshakeComplete())
        {
            handshaker.finishHandshake(chan, (HttpResponse) e.getMessage());
            System.out.println("Handler: client connected");
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
        if (frame instanceof TextWebSocketFrame)
        {
            TextWebSocketFrame text = (TextWebSocketFrame) frame;
            System.out.println("Handler: text message received: " + text.getText());
            // Decrypt and display message here
            msgqueue.add(text.getText());
//            writer.write(text.getText());
        } else if (frame instanceof PingWebSocketFrame)
        {
            System.out.println("Handler: received ping, sending pong");
            chan.write(new PongWebSocketFrame(frame.getBinaryData()));
        } else if (frame instanceof PongWebSocketFrame)
        {
            System.out.println("Handler: received pong");
        } else if (frame instanceof CloseWebSocketFrame)
        {
            System.out.println("Handler: received close");
            chan.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        final Throwable t = e.getCause();
        t.printStackTrace();
        e.getChannel().close();
    }

}
