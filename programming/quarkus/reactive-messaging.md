# Reactive messaging in Quarkus

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
