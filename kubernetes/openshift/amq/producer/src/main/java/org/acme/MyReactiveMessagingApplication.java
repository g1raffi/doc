package org.acme;

import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class MyReactiveMessagingApplication {

    @Inject
    @ConfigProperty(name = "id")
    private String id;

    private static int counter;

    @Inject
    @Channel("words-out")
    Emitter<String> emitter;

    @Scheduled(every = "2s")
    void emitMessage() {
        emitter.send(id + ": #" + counter++);
    }
}
