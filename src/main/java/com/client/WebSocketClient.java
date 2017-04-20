package com.client;

import com.security.rsa.RSA;
import com.security.rsa.RSAKey;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 3/29/17.
 */
public class WebSocketClient
{
    private final URI uri;
    private final ClientBootstrap bootstrap;
    private final String protocol;
    private final WebSocketClientHandshaker handshaker;
    private final ChannelPipelineFactory factory;
    private Channel chan;

    public WebSocketClient(URI uri,
                           String username,
                           BigInteger publicKey)
    {
        this.uri = uri;
        protocol = uri.getScheme();
        chan = null;

        // Create client-side socket channel
        NioClientSocketChannelFactory nio =
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());
        bootstrap = new ClientBootstrap(nio);
        // Create handshake
        WebSocketClientHandshakerFactory handshakerFactory = new WebSocketClientHandshakerFactory();

        // Add custom headers for username and key
        HashMap<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put("X-USER", username);
        customHeaders.put("X-CLIENT-KEY", publicKey.toString());

        handshaker = handshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, customHeaders);

        factory = new ChannelPipelineFactory()
        {
            public ChannelPipeline getPipeline() throws Exception
            {
                ChannelPipeline pipe = Channels.pipeline();
                pipe.addLast("decoder", new HttpResponseDecoder());
                pipe.addLast("encoder", new HttpRequestEncoder());
                pipe.addLast("ws-handler", new WebSocketClientHandler(handshaker));
                return pipe;
            }
        };

        // Create pipelines
        bootstrap.setPipelineFactory(factory);

        // Connect
        try
        {
            connect();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connect() throws Exception
    {
        System.out.println("Connected");
        ChannelFuture future = bootstrap.connect(
            new InetSocketAddress(uri.getHost(), uri.getPort()));
        future.syncUninterruptibly();
        chan = future.getChannel();
        handshaker.handshake(chan).syncUninterruptibly();
    }

    public void sendMessage(String message) throws Exception
    {
        System.out.println("Sending message " + message);
        // Encrypt message here
        System.out.println("Message encrypted " + message);
        chan.write(new TextWebSocketFrame(message));
    }

    public void disconnect()
    {
        System.out.println("Disconnected");
        chan.write(new CloseWebSocketFrame());
        chan.getCloseFuture().awaitUninterruptibly();
        chan.close();
        bootstrap.releaseExternalResources();
    }

    public static void main(String[] args) throws Exception
    {
        RSA rsa = new RSA();
        RSAKey key = new RSAKey();
        WebSocketClient ws = new WebSocketClient(
                new URI("ws://localhost:4000/ws"),
                args[0], new BigInteger("1235823"));
    }
}
