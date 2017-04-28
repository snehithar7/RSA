package com.messages;

import java.math.BigInteger;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class JoinMessage extends Message {
    private final String user;
    private final BigInteger publicModulus;
    private final BigInteger publicExponent;
    private final String type = MessageTypes.JOIN;

    public JoinMessage(String user, BigInteger publicModulus, BigInteger publicExponent) {
        this.user = user;
        this.publicModulus = publicModulus;
        this.publicExponent = publicExponent;
    }

    public String getUser() {
        return user;
    }

    public BigInteger getPublicModulus() {
        return publicModulus;
    }

    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    public String getType() {
        return type;
    }

}
