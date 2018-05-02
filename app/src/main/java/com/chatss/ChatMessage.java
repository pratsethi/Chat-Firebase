package com.chatss;

/**
 * Created by PSethi on 28-Apr-18.
 */

public class ChatMessage {

    private String msg,mUsername,photoUrl;

    public ChatMessage() {
    }

    public ChatMessage(String msg, String mUsername, String photoUrl) {
        this.msg = msg;
        this.mUsername = mUsername;
        this.photoUrl = photoUrl;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
