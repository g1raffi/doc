package org.acme;

import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EventListener {

    private final Logger logger = Logger.getLogger(EventListener.class);

    @Incoming("measurements")
    public CompletionStage<Void> consume(Message<SensorMeasurement> message) {
        IncomingCloudEventMetadata cloudEventMetadata = message.getMetadata(IncomingCloudEventMetadata.class).orElseThrow(() -> new IllegalArgumentException("Expected a CloudEvent!"));
        logger.infof("Received Cloud Events (spec-version: %s): id: '%s', source:  '%s', type: '%s', subject: '%s', payload-message: '%s' ",
                cloudEventMetadata.getSpecVersion(),
                cloudEventMetadata.getId(),
                cloudEventMetadata.getSource(),
                cloudEventMetadata.getType(),
                cloudEventMetadata.getSubject().orElse("no subject"),
                message.getPayload());
        return message.ack();
    }
}
