package org.acme;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataRepository {

    private final PgPool pgPool;

    public DataRepository(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    Multi<Data> findAllData() {
        return pgPool.query("SELECT * FROM jsondata")
                .execute()
                .onItem()
                .transformToMulti(i -> Multi.createFrom().iterable(i))
                .map(i -> new Data(i.getUUID("id").toString(), i.getString("json")));
    }
}
