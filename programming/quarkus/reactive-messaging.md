# Reactive messaging in Quarkus

## SerDes

### JsonB

Serializer in JsonB are per default implemented when adding the jsonb dependency and configuring:

```properties

mp.messaging.outgoing.$CHANNEL.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

```

Deserializer can be implemented the following way:

```java

public class JsonMessageDeserializer extends JsonbDeserializer<JsonMessage> {
    public JsonMessageDeserializer() {
        super(JsonMessage.class);
    }
}

```

### Jackson

Serializer changes to: `io.quarkus.kafka.client.serialization.ObjectMapperSerializer`

Deserializer superclass changes to: `ObjectMapperDeserializer`


## Dynamic emit message

To emit messages to dynamic topics or manipulate other metadata:

```java

@ApplicationScoped
public class KafkaProducer {

    @Channel("events")
    @Inject
    Emitter<String> eventEmitter;
    
    public void emitEvent(String message, String topic) {
        eventEmitter.send(
            Message.of(message).addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                .withTopic(topic)
                .build())
        );
    }
}

```

No need to configure the topic beforehand.

```properties

mp.messaging.outgoing.events.connector=smallrye-kafka

```
