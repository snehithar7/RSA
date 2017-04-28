package com.messages;

import com.google.gson.Gson;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public abstract class Message {

    // Must be overridden for each message to return
    // the message type
    public abstract String getType();

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
