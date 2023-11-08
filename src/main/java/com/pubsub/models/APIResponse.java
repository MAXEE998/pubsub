package com.pubsub.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record APIResponse(String status, String message, Object data) {

    // Serialize the ApiResponse to a JSON string
    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
