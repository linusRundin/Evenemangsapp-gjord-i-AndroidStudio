package com.example.projekt_event_app.user;


public class User {
    private String name;
    private String email;
    private String password;
    private String profile_picture_id;
    private String id;

    /**
     * This class is used to interpret a user. It handles all attributes that a user is capable of
     * having.
     */
    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_picture_id() {
        return profile_picture_id;
    }

    public void setProfile_picture_id(String profile_picture_id) {
        this.profile_picture_id = profile_picture_id;
    }


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "{" +
                "username: " + name + '\'' +
                ", email:" + email + '\'' +
                ", password: " + password + '\'' +
                '}';
    }

}
