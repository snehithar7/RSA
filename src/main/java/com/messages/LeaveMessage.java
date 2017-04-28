package com.messages;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class LeaveMessage extends Message {
    private final String user;
    private final String type = MessageTypes.LEAVE;

    public LeaveMessage(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public String getType() {
        return type;
    }

}
