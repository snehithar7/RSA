package com.client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 4/11/17.
 */
public class AMQPClient {
    private static final Logger logger = Logger.getLogger(WebSocketClientHandler.class.getName());
    private final Channel channel;
    private final String exchangeName = "client-exchange";
    private final String userName;
    private final BigInteger privateKey;
    // Use a hashmap of lists to represent the text sent to/received from each user
    private final HashMap<String, String> messages;

    /**
     * Initialize an AMQP client for consuming/sending messages.
     *
     * @param uri        URI of the producer
     *                   (ex. amqp://userName:password@hostname:portNumber/virtualHost)
     * @param userName   username for this client
     * @param privateKey private key for this client (for decrypting messages)
     */
    public AMQPClient(String uri,
                      String userName,
                      BigInteger privateKey,
                      String[] users) throws Exception {
        this.userName = userName;

        // Store our private key for decrypting
        this.privateKey = privateKey;
        messages = new HashMap<>();

        // Add all users to the list of users
        for (String item : users) {
            messages.put(item, "");
        }

        // Build connection factory
        ConnectionFactory factory = new ConnectionFactory();
        // URI of the format: amqp://userName:password@hostname:portNumber/virtualHost
        factory.setUri(uri);
        Connection conn = factory.newConnection();
        channel = conn.createChannel();

        String queueName = channel.queueDeclare().getQueue();

        // Create direct exchange and queue
        channel.exchangeDeclare(exchangeName, "direct");
        // Listen to our user's queue
        channel.queueBind(queueName, exchangeName, userName);

        Consumer consumerCb = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.log(Level.FINE, "Received message: {0} from {1}",
                        new Object[]{message, envelope.getRoutingKey()});
                // Decrypt the message using our private key
                // message = RSA.decrypt(message, privateKey)
                // Add received message to internal queue
                logger.log(Level.FINE, "Decrypted message: {0}", message);
                // Add message to internal history for this user
                String text = messages.get(envelope.getRoutingKey());
                text += envelope.getRoutingKey() + ": " + message + "\n";
                messages.put(envelope.getRoutingKey(), text);
            }
        };
    }

    /**
     * Send a message to targetUser, encrypting the message with the user's
     * public key.
     *
     * @param targetUser          Target username
     * @param targetUserPublicKey Target user's public key (for encrypting)
     * @param message             Client to send
     */
    public void sendMessage(String targetUser,
                            BigInteger targetUserPublicKey,
                            String message) {
        // Encrypt
        // message = RSA.encrypt(message, targetUserPublicKey)
        logger.log(Level.FINE, "Sending message: {0} to {1}", new Object[]{message, targetUser});
        try {
            channel.basicPublish(exchangeName, targetUser, null, message.getBytes());
            String text = messages.get(targetUser);
            text += targetUser + ": " + message + "\n";
            messages.put(targetUser, text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the text history for target user
     *
     * @param targetUser Target user
     * @return String
     */
    public String getMessages(String targetUser) {
        return messages.get(targetUser);
    }

}
