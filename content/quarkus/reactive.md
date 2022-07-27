---
weight: 23
title: Reactive

---

# Reactive World

## Emit periodic messages

> SomeEmitter.class

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


## Add dependencies

> pom.xml

```xml

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-reactive-pg-client</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-flyway</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive-jsonb</artifactId>
    </dependency>

```

Add dependencies to your code:


### Reactive PgClient

Create a `@ApplicationScoped` repository for the data access:

```java

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

```

### Migration

Create flyway migration script to initialize table and add dummy data:

> src/main/resources/db/migration/V1.0.0__Quarkus.sql

```sql

CREATE TABLE IF NOT EXISTS jsondata
    (
    id      uuid PRIMARY KEY,
    json    text
    );

INSERT INTO jsondata (id, json) VALUES ('21c23437-89d1-4774-bbbe-c286fb7c3afd', 'teststring');

```


### Configuration

Add configuration to enable auto migration:

> `application.property`

```properties

quarkus.flyway.migrate-at-start=true

```


### Rest Resource

Alter the REST resource to return the object from the database:

> DataResource.java

```java

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

```

> Data.java

```java

package org.acme;

public class Data {

    public String id;
    public String data;

    public Data() {
    }

    public Data(String id, String data) {
        this.id = id;
        this.data = data;
    }
}

```
