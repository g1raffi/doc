package org.acme;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Random;

@Path("/measurements")
public class MeasurementsResource {
    private final KafkaProducer kafkaProducer;

    public MeasurementsResource(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @POST
    public Response emitMeasurement() {
        SensorMeasurement measurement = SensorMeasurement.newBuilder().setData(new Random().nextDouble()).build();
        kafkaProducer.emitEvent(measurement);
        return Response.ok().build();
    }
}
