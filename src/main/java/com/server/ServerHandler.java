package com.server;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alec.ferguson on 4/20/2017.
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {
    static Map<String, Client> connectedClients = new HashMap<>();
    private static final String PATH = "/ws";
    private static final String USERS = "/users";
    private static final String KEY_HEADER = "X-CLIENT-KEY";
    private static final String USER_HEADER = "X-USER";
    private static final String EXPONENT_HEADER = "X-CLIENT-EXPONENT";

    @Override
    public void messageReceived(ChannelHandlerContext context,
                                MessageEvent evt)
    {
        Object message = evt.getMessage();
        if (message instanceof WebSocketFrame)
        {
            handleWebsocketFrame(context, (WebSocketFrame) message);
        } else if (message instanceof HttpRequest)
        {
            handleHttpRequest(context, (HttpRequest) message);
        }
    }

    private void handleWebsocketFrame(ChannelHandlerContext context,
                                      WebSocketFrame frame)
    {
        if (frame instanceof TextWebSocketFrame)
        {
            // Parse json frame
            String frameText = ((TextWebSocketFrame) frame).getText();
            System.out.println("Server got message: " + frameText);
            JSONObject object = new JSONObject(frameText);
            String to = object.getString("to");

            // Get the target channel
            Client receiver = connectedClients.get(to);
            // Write the message to the receiver's channel
            if (receiver != null)
            {
                receiver.getChannel().write(frame);
            }
        } else if (frame instanceof CloseWebSocketFrame)
        {
            // Remove client from table
            for (Map.Entry<String, Client> entry :
                    connectedClients.entrySet()) {
                String username = entry.getKey();
                if (entry.getValue().getChannel() ==
                        context.getChannel()){
                    connectedClients.remove(username);
                    System.out.println("User " + username + "disconnected!");
                }
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext context,
                                   HttpRequest request)
    {
        final Channel channel = context.getChannel();

        // Handle websocket connection request.
        if (request.getUri().equals(PATH))
        {
            String username = request.getHeader(USER_HEADER);
            String key = request.getHeader(KEY_HEADER);
            String exponent = request.getHeader(EXPONENT_HEADER);
            ChannelFuture handshake;

            // Close connection if user did not send expected headers
            if (username != null && key != null) {
                // Build upgrade response
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        request.getUri(),
                        null,
                        false);
                final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);

                // Wait for handshake
                handshake = handshaker.handshake(channel, request);
                handshake.awaitUninterruptibly(5);

                if (handshake.isDone()) {
                    System.out.println(
                            "Client connected with name <" + username +
                                    ">, key <" + key + ">, exponent <" +
                                    exponent + ">");
                    connectedClients.put(username,
                            new Client(username,
                                    new BigInteger(key),
                                    new BigInteger(exponent),
                                    channel));
                }
                return;
            }
        } else if (request.getUri().equals(USERS))
        {
            // Display all connected users
            JSONObject json = new JSONObject();
            // Iterate over usermap and get all public keys
            for (Map.Entry<String, Client> entry :
                    connectedClients.entrySet()) {
                json.put(
                        entry.getKey(),
                        entry.getValue().getPublicKey().toString() +
                                ":" +
                        entry.getValue().getExponent().toString());
            }
            // Build response object
            HttpResponse response = new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);
            response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            // Set body to json object
            response.setContent(
                    ChannelBuffers.copiedBuffer(json.toString(), CharsetUtil.UTF_8));
            // Send response
            httpResponse(context,
                    response,
                    request);
            return;
        }
        // Error response on fail
        httpResponse(context,
                new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST),
                request);
        return;
    }

    public void httpResponse(ChannelHandlerContext context,
                             HttpResponse response,
                             HttpRequest request)
    {
        Channel channel = context.getChannel();
        ChannelFuture future;
        // Write response to channel
        future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event)
            throws Exception
    {
        event.getCause().printStackTrace();
        event.getChannel().close();
    }

}
