package com.server;

import com.google.gson.Gson;
import com.messages.ChatMessage;
import com.messages.JoinMessage;
import com.messages.LeaveMessage;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.*;
import com.globals.Globals;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alec.ferguson on 4/20/2017.
 *
 * Handler for the Http server. Serves WebSocket connections on /ws
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {
    private static Map<String, Client> connectedClients = new ConcurrentHashMap<>();

    /** Override the message handler and handle messages based on
     * if they are http requests or WebSocket frames.
     *
     * @param context Channel context.
     * @param evt Message event.
     */
    @Override
    public void messageReceived(ChannelHandlerContext context,
                                MessageEvent evt)
    {
        Object message = evt.getMessage();
        if (message instanceof WebSocketFrame)
        {
            handleWebSocketFrame(context, (WebSocketFrame) message);
        } else if (message instanceof HttpRequest)
        {
            handleHttpRequest(context, (HttpRequest) message);
        }
    }

    /** Handle ws frames (text frames from client->server), mainly
     * chat messages of the form:
     *     {"to": <user1>, "from": <user2>, "message": <message>}
     *
     * @param context Channel context.
     * @param frame WS frame.
     */
    private void handleWebSocketFrame(ChannelHandlerContext context,
                                      WebSocketFrame frame)
    {
        if (frame instanceof TextWebSocketFrame)
        {
            // Parse json frame
            ChatMessage message = new Gson().fromJson(
                    ((TextWebSocketFrame) frame).getText(),
                    ChatMessage.class);
            System.out.println("Server got message: " + message.getMessage());

            // Get the target channel
            Client receiver = connectedClients.get(message.getTo());
            // Write the message to the receiver's channel
            if (receiver != null)
            {
                receiver.getChannel().write(frame);
            }
        } else if (frame instanceof CloseWebSocketFrame)
        {
            boolean userFound = false;
            String username = null;
            // Remove client from table
            for (Map.Entry<String, Client> entry :
                    connectedClients.entrySet()) {
                username = entry.getKey();
                if (entry.getValue().getChannel() ==
                        context.getChannel()){
                    userFound = true;
                    connectedClients.remove(username);
                    System.out.println("User " + username + "disconnected!");
                }
            }
            // Notify other users that this user left.
            if (userFound && username != null) {
                broadcastToClients(
                        new TextWebSocketFrame(
                                new LeaveMessage(username)
                                        .toJson()));
            }
        }
    }

    /** Handle http requests (namely the websocket connection request)
     * from the client->server.
     *
     * @param context Channel context.
     * @param request The client http request.
     */
    private void handleHttpRequest(ChannelHandlerContext context,
                                   HttpRequest request)
    {
        final Channel channel = context.getChannel();

        // Handle websocket connection request.
        if (request.getUri().equals(Globals.WS_PATH))
        {
            // Extract the required key information and username from the
            // websocket headers.
            String username = request.getHeader(Globals.USER_HEADER);
            String key = request.getHeader(Globals.KEY_HEADER);
            String exponent = request.getHeader(Globals.EXPONENT_HEADER);
            ChannelFuture handshake;

            // Only continue if user sent the expected headers
            if (username != null && key != null && exponent != null) {
                // Build upgrade response
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        request.getUri(),
                        null,
                        false);
                final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);

                // Do websocket handshake
                handshake = handshaker.handshake(channel, request);
                handshake.awaitUninterruptibly(5);

                if (handshake.isDone()) {
                    // Log that the user connected
                    System.out.println(
                            "Client connected with name <" + username +
                                    ">, key <" + key + ">, exponent <" +
                                    exponent + ">");
                    Client newClient = new Client(username,
                            new BigInteger(key),
                            new BigInteger(exponent),
                            channel);
                    // Notify this client of all other uses in the channel
                    for (Map.Entry<String, Client> entry :
                            connectedClients.entrySet()) {
                        Client client = entry.getValue();
                        newClient.getChannel().write(
                                new TextWebSocketFrame(
                                        new JoinMessage(
                                                client.getUsername(),
                                                client.getPublicKey(),
                                                client.getExponent())
                                                .toJson()
                                ));
                    }
                    // Add user to the HashMap of connected clients.
                    connectedClients.put(username, newClient);
                    // Notify all other clients that a new user joined
                    broadcastToClients(
                            new TextWebSocketFrame(
                                    new JoinMessage(
                                            newClient.getUsername(),
                                            newClient.getPublicKey(),
                                            newClient.getExponent())
                                    .toJson()
                            ));
                }
                return;
            }
        }
        // Error response on fail
        httpResponse(context,
                new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST));
        return;
    }

    /** Http response builder
     *
     * @param context Channel context.
     * @param response Http response object to return.
     */
    public void httpResponse(ChannelHandlerContext context,
                             HttpResponse response)
    {
        Channel channel = context.getChannel();
        ChannelFuture future;
        // Write response to channel
        future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    /** Broadcast a websocket frame to all connected clients.
     *
     * @param frame Frame
     */
    public void broadcastToClients(WebSocketFrame frame)
    {
        for (Map.Entry<String, Client> entry :
                connectedClients.entrySet()) {
            entry.getValue().getChannel().write(frame);
        }
    }

    /** Exception handler for this module.
     *
     * @param context
     * @param event
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event)
            throws Exception
    {
        event.getCause().printStackTrace();
        event.getChannel().close();
    }

}
