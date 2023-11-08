package com.pubsub.websockets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.pubsub.models.APIResponse;
import com.pubsub.services.SubscriberService;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebSocket
public class SubscriberSession {
    // static fields
    private final Logger logger = LoggerFactory.getLogger(SubscriberSession.class);
    private final SubscriberService subscriberService;

    public SubscriberSession(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @OnWebSocketConnect
    public void onConnect(@NotNull Session session) {
        try {
            var subscriber = subscriberService.createSubscriberFromSession(session);
            APIResponse response = new APIResponse("successful", "connection", subscriber.getID());
            session.getRemote().sendString(response.toJson());
        } catch (IOException e) {
            logger.error("IOException:", e);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        var subscriber = subscriberService.removeSubscriber(session);
        logger.info("Subscriber {} WebSocket closed. Status code: {}, Reason: {}", subscriber.getID(), statusCode, reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.info(String.format("Active Session Count %d", subscriberService.getActiveSessionCount()));
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String messageType = json.get("type").getAsString();
            try {
                // Dispatch the message to the appropriate handler based on the request type
                switch (messageType) {
                    case "subscribe": {
                        String topic = json.get("topic").getAsString();
                        subscriberService.handleSubscribeRequest(session, topic);
                        break;
                    }
                    case "unsubscribe": {
                        String topic = json.get("topic").getAsString();
                        subscriberService.handleUnsubscribeRequest(session, topic);
                        break;
                    }
                    default: {
                        APIResponse response = new APIResponse("failed", "Unknown Operation", null);
                        session.getRemote().sendString(response.toJson());
                    }
                }
            } catch (JsonSyntaxException e) {
                APIResponse response = new APIResponse("failed", "Incorrect request syntax", null);
                session.getRemote().sendString(response.toJson());
            }
        } catch (IOException e) {
            logger.error("IOException:", e);
        }
    }
}
