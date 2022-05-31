package com.example.projekt_event_app.commments;

import com.example.projekt_event_app.commments.Comment;

import java.util.ArrayList;

/**
 * used ass a arraylist of comments to receive comments from the server.
 */
public class Comments {

    private ArrayList<Comment> comments;

    public Comments() {
        this.comments = new ArrayList<>();
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void getComment(int i){
        comments.get(i);
    }

    public ArrayList<Comment> getComments(){
        return comments;


    }


}
