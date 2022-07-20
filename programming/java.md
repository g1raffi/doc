# Java

## Quarkus

### Periodically triggered messages

```java
    @ConfigProperty(name = "application.polling.interval-seconds", defaultValue
= "900")
    int pollingInterval;
    
    @Outgoing("Trigger")
    public Flowable<String> triggerUpdateEvent() {
        return Flowable.interval(pollingInterval, TimeUnit.SECONDS)
            .map(tick -> "triggered");
    }

    @Incoming("Trigger")
    @Outgoing("InboundReading")
    public PublisherBuilder<List<E>> update(String triggered) {
        log.debug("trigger received");
        E e = result();
        return ReactiveStreams.of(e);
    }
```
