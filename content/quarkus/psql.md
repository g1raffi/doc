---
weight: 21
title: Reactive

---

# Reactive psql client

```shell
./mvnw quarkus:add-extension -Dextensions="reactive-pg-client"
```

Starts automatically new devservice psql container with db/user/password = quarkus.

```shell
root@ac1e2f26032a:/# psql -U quarkus -d quarkus -h localhost -p 5432
```

# Create InitDB Skript like

```java

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;
    private static final Logger log = Logger.getLogger(DBInit.class.getName());

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            log.info("Initializing Database");
            initDb();
        }
    }

    private void initDb() {
        client.query("DROP TABLE IF EXISTS sensormeasurements").execute()
                .flatMap(r -> client.query("CREATE TABLE sensormeasurements (id SERIAL PRIMARY KEY, data DOUBLE PRECISION, time TIMESTAMP WITH TIME ZONE DEFAULT NOW()::timestamp)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.1)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.2)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.3)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.4)").execute())
                .await().indefinitely();
    }
}

```


# CRUD Operations example

```java
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.time.Instant;
import java.time.ZoneOffset;

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }


    public SensorMeasurement(Row row) {
        this.id = row.getLong("id");
        this.data = row.getDouble("data");
        this.time = Instant.from(row.getOffsetDateTime("time"));
    }

    public static Multi<SensorMeasurement> findAll(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(SensorMeasurement::new);
    }

    public static Uni<SensorMeasurement> findById(PgPool client, Long id) {
        return client.preparedQuery("SELECT id, data, time from sensormeasurements where id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }

    public static Uni<SensorMeasurement> findLatest(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements ORDER BY time DESC LIMIT 1").execute()
                .map(RowSet::iterator)
                .map(rowRowIterator -> rowRowIterator.hasNext() ? new SensorMeasurement(rowRowIterator.next()) : null);
    }

    public Uni<SensorMeasurement> save(PgPool client) {
        return client.preparedQuery("INSERT INTO sensormeasurements (data, time) VALUES ($1, $2) RETURNING (id, data, time)")
                .execute(Tuple.of(data, time.atOffset(ZoneOffset.UTC)))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? this : null);
    }
}
```
