package com.pubsub.services;

import com.pubsub.models.APIResponse;
import com.pubsub.models.Subscriber;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriberService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriberService.class);
    private final ConcurrentMap<Session, Subscriber> sessionSubscribers = new ConcurrentHashMap<>();

    private final EventService eventService;

    public SubscriberService(EventService eventService) {
        this.eventService = eventService;
    }

    public int getActiveSessionCount() {
        return sessionSubscribers.size();
    }

    public Subscriber getSubscriberBySession(Session session) {
        return sessionSubscribers.get(session);
    }

    public Subscriber createSubscriberFromSession(Session session) {
        var subscriber = new Subscriber();
        sessionSubscribers.put(session, subscriber);
        logger.info(String.format("Subscriber %s created", subscriber.getID()));
        return subscriber;
    }

    public Subscriber removeSubscriber(Session session) {
        return sessionSubscribers.remove(session);
    }

    public void handleSubscribeRequest(@NotNull Session session, @NotNull String topic) throws IOException {
        var subscriber = this.getSubscriberBySession(session);
        APIResponse response;

        if (subscriber == null) {
            response = new APIResponse("failed", "session expired", null);
            session.getRemote().sendString(response.toJson());
            return;
        }

        var iterator = eventService.getTopicIterator(topic);

        if (iterator == null) {
            response = new APIResponse("failed", "topic not exist", null);
        } else {
            subscriber.subscribeToTopic(topic, iterator);
            response = new APIResponse(
                    "success",
                    String.format("topic %s subscribed", topic),
                    null);
        }
        session.getRemote().sendString(response.toJson());
    }

    public void handleUnsubscribeRequest(@NotNull Session session, @NotNull String topic) throws IOException {
        var subscriber = this.getSubscriberBySession(session);
        APIResponse response;

        if (subscriber == null) {
            response = new APIResponse("failed", "session expired", null);
            session.getRemote().sendString(response.toJson());
            return;
        }

        var iterator = subscriber.unsubscribeFromTopic(topic);

        if (iterator == null) {
            response = new APIResponse("failed", "topic not subscribed", null);
        } else {
            response = new APIResponse(
                    "success",
                    String.format("topic %s unsubscribed", topic),
                    null);
        }
        session.getRemote().sendString(response.toJson());
    }

    public void publishEvent(String topic) {
        Thread thread = new Thread(() -> {
            logger.info(String.format("Pushing events on Topic %s...", topic));
            for (var entry : sessionSubscribers.entrySet()) {
                var subscriber = entry.getValue();
                var session = entry.getKey();
                var iterator = subscriber.getTopicIterator(topic);
                if (iterator != null) {
                    if (session.isOpen()) {
                        try {
                            var events = iterator.getEvents();
                            if (!events.isEmpty()) {
                                logger.info(String.format("Pushed %d events on Topic %s to Subscriber %s", events.size(), topic, subscriber.getID()));
                                APIResponse apiResponse = new APIResponse("successful", "Events push for " + topic, events);
                                session.getRemote().sendString(apiResponse.toJson());
                                iterator.setLastSequenceNumber(events.get(events.size()-1).getSequenceNumber()+1);
                            }
                        } catch (IOException e) {
                            logger.error("IOException:", e);
                        }
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
