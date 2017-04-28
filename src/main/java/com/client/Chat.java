package com.client;

import com.messages.*;
import com.security.rsa.RSA;
import com.security.rsa.RSAKey;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class Chat {
    public Set<String> users;
    // Map of connected users
    private Map<String, ChatUser> connectedUsersMap = new ConcurrentHashMap<>();
    private WebSocketClient client;
    private ChatUser activeUser;

    /**
     * Class for managing chat history for each user and sending/receiving
     * chat messages. Wraps the chat WebSocket client.
     *
     * @param userName
     * @param serverUri
     * @param rsa
     * @param rsakey
     */
    public Chat(String userName,
                URI serverUri,
                RSA rsa,
                RSAKey rsakey) {
        // Initialize websocket client and send our
        // public key
        client = new WebSocketClient(serverUri,
                userName,
                rsakey.getBigPrime(),
                rsakey.getExponent());
        // Initialize the active user
        activeUser = new ChatUser(userName,
                rsa,
                rsakey);
    }

    /**
     * Send a chat message to another user.
     *
     * @param message
     * @param targetUser
     */
    public void sendChatMessage(String message,
                                String targetUser) {
        // Find target user
        ChatUser user = connectedUsersMap.get(targetUser);
        // Send encrypted message
        sendMessage(
                new ChatMessage(
                        targetUser,
                        activeUser.getUsername(),
                        user.getRsa().encrypt(message))
                        .toJson());
    }

    /**
     * Internal method for sending all messages
     *
     * @param message
     */
    private void sendMessage(String message) {
        try {
            client.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read all pending messages from the message queue
     */
    public void readMessages() {
        try {
            while (client.msgqueue.peek() != null) {
                String messageString = client.msgqueue.remove();
                // Remove the next message from the queue
                JSONObject json = new JSONObject(messageString);
                // Get the type of the message
                String type = json.get("type").toString();

                // Get the correct message type from the factory.
                MessageFactory messageFactory = new MessageFactory();
                Message message = messageFactory.getMessage(type, messageString);

                if (message instanceof JoinMessage) {
                    handleJoinMessage((JoinMessage) message);
                } else if (message instanceof LeaveMessage) {
                    handleLeaveMessage((LeaveMessage) message);
                } else if (message instanceof ChatMessage) {
                    handleChatMessage((ChatMessage) message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the map of connected users.
     *
     * @return
     */
    public Map<String, ChatUser> getConnectedUsersMap() {
        return connectedUsersMap;
    }

    /**
     * Handle a 'join' message (a user joins the chat).
     *
     * @param message
     */
    public void handleJoinMessage(JoinMessage message) {
        // Construct an RSA object for the new user with the provided
        // exponent and public mod.
        RSAKey rsaKey = new RSAKey();
        rsaKey.setBigPrime(message.getPublicModulus());
        rsaKey.setExponent(message.getPublicExponent());
        RSA rsa = new RSA(rsaKey);

        // Add the user to the connected users map.
        connectedUsersMap.put(message.getUser(),
                new ChatUser(message.getUser(), rsa, rsaKey));
    }

    /**
     * Handle a 'leave' message (a user has left the chat).
     *
     * @param message
     */
    public void handleLeaveMessage(LeaveMessage message) {
        // Remove the user from the map.
        connectedUsersMap.remove(message.getUser());
    }

    /**
     * Handle a chat message from a given user.
     * Update the chat history between this user and ourselves
     * to add the new message.
     *
     * @param message
     */
    public void handleChatMessage(ChatMessage message) {
        // Decrypt message
        String msg = activeUser.getRsa().decrypt(message.getMessage());

        // Get the user that the message is from
        ChatUser from = connectedUsersMap.get(message.getFrom());
        // Update the existing chat history with a new line of chat text
        from.setChatHistory(from.getChatHistory() +
                from.getUsername() + ": " + msg + "\n");
    }
}
