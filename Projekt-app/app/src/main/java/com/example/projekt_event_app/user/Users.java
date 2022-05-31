package com.example.projekt_event_app.user;

import java.util.ArrayList;

/**
 * This is the users class. It handles the ArrayList with users in it.
 */
public class Users {

    private ArrayList<User> users;

    public Users() {
        this.users = new ArrayList<>();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public User getUser(int i) {
        return users.get(i);
    }

    public void addUser(User user){
        users.add(user);

    }
}
