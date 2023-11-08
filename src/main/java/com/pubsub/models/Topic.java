package com.pubsub.models;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class Topic {
    private final String name;
    private final ArrayList<Event> eventQueue;
    private int sequenceNumber;
    private final Map<UUID, Event> eventMap;
    private final ReentrantReadWriteLock lock;
    private final ReadLock readLock;
    private final WriteLock writeLock;

    public Topic(String name) {
        this.name = name;
        this.eventQueue = new ArrayList<>();
        this.sequenceNumber = 0;
        this.eventMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    public Event addEvent(@NotNull Event event) {
        writeLock.lock();
        Event gotEvent;
        try {
            gotEvent = eventMap.get(event.getId());
            if (gotEvent == null) {
                event.setSequenceNumber(sequenceNumber++);
                eventQueue.add(event);
                eventMap.put(event.getId(), event);
                return event;
            }
        } finally {
            writeLock.unlock();
        }
        return gotEvent;
    }

    public Event getEventByUUID(UUID eventId) {
        readLock.lock();
        try {
            return eventMap.get(eventId);
        } finally {
            readLock.unlock();
        }
    }

    public EventIterator getIterator() {
        readLock.lock();
        try {
            return new EventIterator();
        } finally {
            readLock.unlock();
        }
    }

    public String getName() {
        return name;
    }

    public long getSequenceNumber() {
        readLock.lock();
        try {
            return sequenceNumber;
        } finally {
            readLock.unlock();
        }
    }

    public class EventIterator {
        private int lastSequenceNumber = 0;

        public void setLastSequenceNumber(int i) {
            this.lastSequenceNumber = i;
        }

        public int getLastSequenceNumber() {
            return lastSequenceNumber;
        }

        public List<Event> getEvents() {
            readLock.lock();
            try {
                List<Event> events = new ArrayList<>();
                for (int i = lastSequenceNumber; i < eventQueue.size(); i++) {
                    Event event = eventQueue.get(i);
                    events.add(event);
                }
                return events;
            } finally {
                readLock.unlock();
            }
        }
    }
}
