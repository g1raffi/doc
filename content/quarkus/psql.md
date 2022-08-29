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
