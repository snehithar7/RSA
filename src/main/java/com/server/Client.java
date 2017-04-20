package com.server;

import org.jboss.netty.channel.Channel;

/**
 * Created by alec.ferguson on 4/20/2017.
 */
public class Client {
    private final String username;
    private final String publicKey;
    private Channel channel;

    public Client(String username,
                  String publicKey,
                  Channel channel) {
        this.username = username;
        this.publicKey = publicKey;
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Channel getChannel() {return channel; }
}
