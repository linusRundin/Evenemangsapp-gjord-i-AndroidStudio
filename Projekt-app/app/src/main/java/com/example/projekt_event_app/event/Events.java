package com.example.projekt_event_app.event;

import java.util.ArrayList;

/**
 * A class used to receive events from the server.
 */
public class Events {

    private ArrayList<Event> events;

    public Events() {
        this.events = new ArrayList<Event>();
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event){
        events.add(event);
    }

    public Event getEvent(int i){
        return events.get(i);
    }
}
