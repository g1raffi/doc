package org.acme;

import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.UUID;

@ApplicationScoped
public class KafkaProducer {

    @Channel("measurements")
    @Inject
    Emitter<SensorMeasurement> sensorMeasurementEmitter;

    public void emitEvent(SensorMeasurement sensorMeasurement) {
        OutgoingCloudEventMetadata<Object> metadata = OutgoingCloudEventMetadata.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("event-producer"))
                .withType("measurement-emitted")
                .withSubject("subject-123")
                .build();
        sensorMeasurementEmitter.send(Message.of(sensorMeasurement).addMetadata(metadata));
    }
}
