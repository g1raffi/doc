package org.acme;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @Inject
    DataRepository dataRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Data> hello() {
        return dataRepository.findAllData();
    }
}