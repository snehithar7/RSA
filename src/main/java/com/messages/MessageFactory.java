package com.messages;

import com.google.gson.Gson;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class MessageFactory {

    /** Factory for creating messages based on the message type.
     *
     * @param messageType
     * @param message
     * @return
     */
    public Message getMessage(String messageType,
                              String message) {
        Gson gson = new Gson();
        switch(messageType){
            case MessageTypes.JOIN:
                return gson.fromJson(message, JoinMessage.class);
            case MessageTypes.LEAVE:
                return gson.fromJson(message, LeaveMessage.class);
            case MessageTypes.CHAT:
                return gson.fromJson(message, ChatMessage.class);
        }
        return null;
    }

}
