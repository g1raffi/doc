---
weight: 21
title: Reactive

---


# PSQL Quarkus

## Reactive psql client

```shell
./mvnw quarkus:add-extension -Dextensions="reactive-pg-client"
```

Starts automatically new devservice psql container with db/user/password = quarkus.

```shell
root@ac1e2f26032a:/# psql -U quarkus -d quarkus -h localhost -p 5432
```

## Create InitDB Skript like

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

## CRUD Operations example

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

## Generic Map<String, String> jsonb types

Add dependencies:

```xml
 <dependencies>
  <dependency>
   <groupId>io.quarkus</groupId>
   <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
  </dependency>
  <dependency>
   <groupId>io.quarkus</groupId>
   <artifactId>quarkus-flyway</artifactId>
  </dependency>
  <dependency>
   <groupId>io.quarkus</groupId>
   <artifactId>quarkus-hibernate-orm-panache</artifactId>
  </dependency>
  <dependency>
   <groupId>io.quarkiverse.hibernatetypes</groupId>
   <artifactId>quarkus-hibernate-types</artifactId>
   <version>0.2.0</version>
  </dependency>
  <dependency>
   <groupId>io.quarkus</groupId>
   <artifactId>quarkus-resteasy-reactive</artifactId>
  </dependency>
  <dependency>
   <groupId>io.quarkus</groupId>
   <artifactId>quarkus-jdbc-postgresql</artifactId>
  </dependency>
```

Create migration skripts "/resources/db/migration":

**V1.0.0__Model.sql**:

```sql
CREATE TABLE public.example
(
  id integer NOT NULL,
  content jsonb,
  CONSTRAINT id_pkey PRIMARY KEY (id)
)
```

**V1.0.1__Data.sql**:

```sql
INSERT INTO public.example (id, content) VALUES (1, '{ "field": "value" }');
INSERT INTO public.example (id, content) VALUES (2, '{ "anotherField": "anotherValue" }');
```

Create and define entity:

```java
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Example extends PanacheEntityBase {

    @Id
    public int id;

    @Type(type = JsonTypes.JSON_BIN)
    public Map<String, String> content;
}
```

Enjoy!
