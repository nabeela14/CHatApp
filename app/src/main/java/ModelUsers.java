package com.chatmaster.myblufly;

public class ModelUsers {
    String username,dp,email,about,uid;


    public ModelUsers() {

    }

    public ModelUsers(String username, String dp, String email, String about, String uid) {
        this.username = username;
        this.dp = dp;
        this.email = email;
        this.about = about;
        this.uid = uid;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    }


