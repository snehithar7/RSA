package com.server;

import org.jboss.netty.channel.Channel;

import java.math.BigInteger;

/**
 * Created by alec.ferguson on 4/20/2017.
 */
public class Client {
    private final String username;
    private final BigInteger publicKey;
    private final BigInteger exponent;
    private Channel channel;

    public Client(String username,
                  BigInteger publicKey,
                  BigInteger exponent,
                  Channel channel) {
        this.username = username;
        this.publicKey = publicKey;
        this.exponent = exponent;
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public Channel getChannel() {
        return channel;
    }
}
