package com.pubsub.controllers;

import com.pubsub.services.SubscriberService;
import com.pubsub.websockets.SubscriberSession;
import com.pubsub.models.APIResponse;
import com.pubsub.services.EventService;
import com.pubsub.models.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

public class PublisherController {
    private final Logger logger = LoggerFactory.getLogger(PublisherController.class);
    private EventService eventService;
    private SubscriberService subscriberService;
    public PublisherController(EventService eventService, SubscriberService subscriberService) {
        this.eventService = eventService;
        this.subscriberService = subscriberService;
    }

    public Route publishEvent = (Request req, Response res) -> {
        String topic = req.params(":topic");
        String jsonBody = req.body();

        // Parse the JSON body into an Event object
        Event event = Event.fromJson(jsonBody);
        if (event == null) {
            res.status(400);
            APIResponse response = new APIResponse("failed", "Invalid JSON format for the event.", null);
            return response.toJson();
        }

        // Add the event to the specified topic
        var gotEvent = eventService.addEventToTopic(event, topic);
        logger.info(String.format("Event seq %d created in Topic %s", gotEvent.getSequenceNumber(), topic));

        subscriberService.publishEvent(topic);

        res.status(201); // 201 Created
        APIResponse response = new APIResponse("successful", String.format("Event published to %s", topic), gotEvent);
        return response.toJson();
    };
}