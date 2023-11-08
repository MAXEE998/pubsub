package com.pubsub;

import com.pubsub.controllers.PublisherController;
import com.pubsub.services.EventService;
import com.pubsub.services.SubscriberService;
import com.pubsub.websockets.SubscriberSession;

import static spark.Spark.*;

public class Server {
    public static void main(String[] args) {
        // Initialize EventService
        EventService eventService = new EventService();

        // Initialize SubscriberService
        SubscriberService subscriberService = new SubscriberService(eventService);

        // Initialize PublisherController with EventService dependency
        PublisherController publisherController = new PublisherController(eventService, subscriberService);

        // Set your desired port
        port(8080);

        webSocket("/subscriber", new SubscriberSession(subscriberService));
        // Define the routes
        path("/publish", () -> {
            post("/:topic", publisherController.publishEvent);
        });

        // Start Spark server
        init();
    }
}
