package com.example.projekt_event_app.commments;

/**
 * A class used to communicate with the server, to receive and return information used to with comments.
 */
public class Comment {

    private String content;
    private String user;
    private String user_pic_id;


    public Comment() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser_pic_id() {
        return user_pic_id;
    }

    public void setUser_pic_id(String user_pic_id) {
        this.user_pic_id = user_pic_id;
    }
}
