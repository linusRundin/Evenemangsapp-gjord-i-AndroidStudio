package com.example.projekt_event_app.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Used when receiving an event from the server.
 */
public class Event {
    private String name;
    private String start_date;
    private String time;
    private String description;
    private String host;
    private String image_id;
    private List attendance;
    private int id;
    private String location;

    public Event() {

    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List getAttendance() {
        return attendance;
    }

    public void setAttendence(List attendance) {
        this.attendance = attendance;
    }

    public String getImageId() {
        return image_id;
    }

    public void setImageId(String imageId) {
        this.image_id = imageId;
    }

    public String getName() {
        return name;
    }

    public String getStart_Date() {
        return start_date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getHost() {
        return host;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStart_Date(String start_date) {
        this.start_date = start_date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
