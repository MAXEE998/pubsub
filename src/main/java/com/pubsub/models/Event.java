package com.pubsub.models;

import com.google.gson.Gson;

import java.util.UUID;
import java.time.Instant;

public class Event {
    private UUID id;
    private int sequenceNumber;
    private String message;

    public Event() {
        this.id = UUID.randomUUID();
    }

    public Event(String message) {
        this();
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getMessage() {
        return message;
    }

    // Serialize an Event to JSON
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // Deserialize JSON to create an Event
    public static Event fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Event.class);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", sequenceNumber=" + sequenceNumber +
                ", message='" + message + '\'' +
                '}';
    }
}