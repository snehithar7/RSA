package com.client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
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
    private final HashMap<String, LinkedBlockingQueue<String>> messages;

    /** Initialize an AMQP client for consuming/sending messages.
     *
     * @param uri URI of the producer
     *     (ex. amqp://userName:password@hostname:portNumber/virtualHost)
     * @param userName username for this client
     * @param privateKey private key for this client (for decrypting messages)
     * @throws Exception
     */
    public AMQPClient(String uri,
                      String userName,
                      BigInteger privateKey,
                      String[] users) throws Exception {
        this.userName = userName;

        // Store our private key for decrypting
        this.privateKey = privateKey;
        messages = new HashMap<String, LinkedBlockingQueue<String>>();

        // Add all users to the list of users
        for (String item : users) {
            messages.put(item, new LinkedBlockingQueue<String>());
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
                // Add message to internal queue for this user
                messages.get(envelope.getRoutingKey()).add(envelope.getRoutingKey() + ": " + message);
            }
        };
    }

    /** Send a message to targetUser, encrypting the message with the user's
     *  public key.
     * @param targetUser Target username
     * @param targetUserPublicKey Target user's public key (for encrypting)
     * @param message Message to send
     * @throws IOException
     */
    public void sendMessage(String targetUser,
                            BigInteger targetUserPublicKey,
                            String message) {
        // Encrypt
        // message = RSA.encrypt(message, targetUserPublicKey)
        logger.log(Level.FINE, "Sending message: {0} to {1}", new Object[]{message, targetUser});
        try {
            channel.basicPublish(exchangeName, targetUser, null, message.getBytes());
            messages.get(targetUser).add(userName + ": " + message);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /** Return the list of messages between us and another client
     *
     * @param targetUser
     * @return
     */
    public Queue<String> getMessages(String targetUser){
        return messages.get(targetUser);
    }

}
