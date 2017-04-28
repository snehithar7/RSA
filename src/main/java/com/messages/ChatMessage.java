package com.messages;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class ChatMessage extends Message {
    private final String to;
    private final String from;
    private final String message;
    private final String type = MessageTypes.CHAT;

    public ChatMessage(String to, String from, String message) {
        this.to = to;
        this.from = from;
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

}
