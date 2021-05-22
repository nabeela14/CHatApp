package com.chatmaster.myblufly;

public class BTModelChat {
    int sender;
    int receiver;
    String msg;
    long time;
    String address;

    public BTModelChat(int sender, int receiver, String msg, long time, String address) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.time = time;
        this.address = address;
    }

    public BTModelChat(long time, String writeMessage, int i, String mConnectedDeviceAddress) {
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

