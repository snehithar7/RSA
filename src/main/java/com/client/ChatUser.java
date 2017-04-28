package com.client;

import com.security.rsa.RSA;
import com.security.rsa.RSAKey;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class ChatUser {
    private String username;
    private String chatHistory;
    private RSA rsa;
    private RSAKey rsaKey;

    public ChatUser(String username,
                    RSA rsa,
                    RSAKey rsaKey) {
        this.username = username;
        this.chatHistory = "";
        this.rsa = rsa;
        this.rsaKey = rsaKey;
    }

    public String getUsername() {
        return username;
    }

    public String getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(String chatHistory) {
        this.chatHistory = chatHistory;
    }

    public RSA getRsa() {
        return rsa;
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }
}
