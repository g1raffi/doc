package org.acme;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Identifier;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class MyReactiveMessagingApplication {

    @Inject
    @ConfigProperty(name = "id")
    private String id;

    private static int counter;

    @Inject
    @Channel("words")
    Emitter<String> emitter;

    @Scheduled(every = "2s")
    void emitMessage() {
        emitter.send(id + ": #" + counter++);
    }

    @Produces
    @Identifier("tls")
    public AmqpClientOptions getNamedOptions() {
        // You can use the produced options to configure the TLS connection
        PemKeyCertOptions keycert = new PemKeyCertOptions()
                .addCertPath("./tls/tls.crt")
                .addKeyPath("./tls/tls.key");
        PemTrustOptions trust = new PemTrustOptions().addCertPath("./tlc/ca.crt");
        return new AmqpClientOptions()
                .setSsl(true)
                .setPemKeyCertOptions(keycert)
                .setPemTrustOptions(trust)
                .addEnabledSaslMechanism("EXTERNAL")
                .setHostnameVerificationAlgorithm("")
                .setConnectTimeout(30000)
                .setReconnectInterval(5000);
    }
}
