package com.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 3/29/17.
 */
public class WebSocketClient {
    private static final Logger logger = Logger.getLogger(WebSocketClientHandler.class.getName());
    private final URI uri;
    private final ClientBootstrap bootstrap;
    private final String protocol;
    private final WebSocketClientHandshaker handshaker;
    private final ChannelPipelineFactory factory;
    private Channel chan;

    public WebSocketClient(URI uri) throws Exception {
        this.uri = uri;
        this.protocol = uri.getScheme();
        this.chan = null;

        // Create client-side socket channel
        NioClientSocketChannelFactory nio =
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());
        this.bootstrap = new ClientBootstrap(nio);
        // Create handshake
        WebSocketClientHandshakerFactory handshakerFactory = new WebSocketClientHandshakerFactory();
        this.handshaker = handshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null);

        this.factory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipe = Channels.pipeline();
                pipe.addLast("decoder", new HttpResponseDecoder());
                pipe.addLast("encoder", new HttpRequestEncoder());
                pipe.addLast("ws-handler", new WebSocketClientHandler(handshaker));
                return pipe;
            }
        };

        // Create pipelines
        this.bootstrap.setPipelineFactory(this.factory);

        // Connect
        this.connect();

    }

    private void connect() throws Exception {
        logger.log(Level.FINE, "Client connecting");
        ChannelFuture future = this.bootstrap.connect(
            new InetSocketAddress(this.uri.getHost(), this.uri.getPort()));
        future.syncUninterruptibly();
        this.chan = future.getChannel();
        this.handshaker.handshake(this.chan).syncUninterruptibly();
    }

    public void sendMessage(String message) throws Exception {
        logger.log(Level.ALL, "Sending message <{0}>", message);
        // Encrypt message here
        logger.log(Level.ALL, "Message encrypted <{0}>", message);
        this.chan.write(new TextWebSocketFrame(message));
    }

    public void disconnect() {
        logger.log(Level.ALL, "Closing websocket connection");
        this.chan.write(new CloseWebSocketFrame());
        this.chan.getCloseFuture().awaitUninterruptibly();
        this.chan.close();
        this.bootstrap.releaseExternalResources();
    }

    public static void main(String[] args) throws Exception {
        URI uri;
        if (args.length > 0) {
            uri = new URI(args[0]);
        } else {
            uri = new URI("ws://localhost:8080/websocket");
        }

        WebSocketClient ws = new WebSocketClient(uri);
        ws.sendMessage("Test");
        ws.disconnect();
    }

}
