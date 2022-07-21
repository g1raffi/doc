package org.acme;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class JsonMessageDeserializer extends JsonbDeserializer<JsonMessage> {
    public JsonMessageDeserializer() {
        super(JsonMessage.class);
    }
}
