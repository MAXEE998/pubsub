package com.pubsub.services;

import com.pubsub.models.Event;
import com.pubsub.models.Topic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class EventService {
    private final Map<String, Topic> topics;

    public EventService() {
        this.topics = new ConcurrentHashMap<>();
    }

    public Event addEventToTopic(Event event, String topicName) {
        return topics
                .computeIfAbsent(topicName, k -> new Topic(topicName))
                .addEvent(event);
    }

    public Event getEventByTopicAndUUID(String topicName, UUID eventId) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            return topic.getEventByUUID(eventId);
        }
        return null;
    }

    public Topic.EventIterator getTopicIterator(String topicName) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            return topic.getIterator();
        }
        return null;
    }

    public String[] getTopicNames() {
        return topics.keySet().toArray(new String[0]);
    }
}
