package com.client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alec on 4/11/17.
 */
public class AMQPClient {
    private static final Logger logger = Logger.getLogger(WebSocketClientHandler.class.getName());
    private final Channel channel;
    private final String exchangeName = "client-exchange";
    private final BigInteger privateKey;

    /** Initialize an AMQP client for consuming/sending messages.
     *
     * @param uri URI of the producer
     *     (ex. amqp://userName:password@hostname:portNumber/virtualHost)
     * @param userName username for this client
     * @param privateKey private key for this client (for decrypting messages)
     * @throws Exception
     */
    public AMQPClient(String uri, String userName, BigInteger privateKey) throws Exception {
        // Store our private key for decrypting
        this.privateKey = privateKey;

        // Build connection factory
        ConnectionFactory factory = new ConnectionFactory();
        // URI of the format: amqp://userName:password@hostname:portNumber/virtualHost
        factory.setUri(uri);
        Connection conn = factory.newConnection();
        this.channel = conn.createChannel();

        String queueName = channel.queueDeclare().getQueue();

        // Create direct exchange and queue
        this.channel.exchangeDeclare(this.exchangeName, "direct");
        // Listen to our user's queue
        this.channel.queueBind(queueName, exchangeName, userName);

        Consumer consumerCb = new DefaultConsumer(this.channel) {
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
                // publish message to UI
                // messages.callBack(envelope.getRoutingKey(), message)
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
                            String message) throws IOException {
        // Encrypt
        // message = RSA.encrypt(message, targetUserPublicKey)
        logger.log(Level.FINE, "Sending message: {0} to {1}", new Object[]{message, targetUser});
        this.channel.basicPublish(this.exchangeName, targetUser, null, message.getBytes());
    }

}
