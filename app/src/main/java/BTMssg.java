package com.chatmaster.myblufly;

public class BTMssg {

    String mssg;
    String type;

    public BTMssg(String mssg, String type) {
        this.mssg = mssg;
        this.type = type;
    }

    public String getMssg() {
        return mssg;
    }

    public void setMssg(String mssg) {
        this.mssg = mssg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
