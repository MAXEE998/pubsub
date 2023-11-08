package com.pubsub.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Subscriber {
    private final Logger logger = LoggerFactory.getLogger(Subscriber.class);

    // object fields
    private final Map<String, Topic.EventIterator> subscribeTopics;
    private final UUID ID;

    public Subscriber() {
        this.ID = UUID.randomUUID();
        this.subscribeTopics = new HashMap<>();
    }

    public Map<String, Topic.EventIterator> getSubscribeTopics() {
        return subscribeTopics;
    }

    public Topic.EventIterator getTopicIterator(String topic) {
        return subscribeTopics.get(topic);
    }

    public void subscribeToTopic(String topic, Topic.EventIterator iterator) {
        logger.info(String.format("Subscriber %s has subscribed to %s", ID, topic));
        subscribeTopics.put(topic, iterator);
    }

    public Topic.EventIterator unsubscribeFromTopic(String topic) {
        logger.info(String.format("Subscriber %s has unsubscribed from %s", ID, topic));
        return subscribeTopics.remove(topic);
    }

    public UUID getID() {
        return ID;
    }
}
