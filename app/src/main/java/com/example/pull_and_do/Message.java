package com.example.pull_and_do;

import java.util.Date;

public class Message {
    private String nameUser;
    private String textMessage;
    private long timeMessage;

    public Message() { }

    public Message(String nameUser, String textMessage) {
        this.nameUser = nameUser;
        this.textMessage = textMessage;
        this.timeMessage = new Date().getTime();
    }

    public String getName() {
        return nameUser;
    }

    public String getText() {
        return textMessage;
    }

    public long getTimeMessage() {
        return timeMessage;
    }

    public void setName(String name) {
        this.nameUser = name;
    }

    public void setText(String textMessage) {
        this.textMessage = textMessage;
    }

}
