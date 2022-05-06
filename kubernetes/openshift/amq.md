# AMQ Openshift Setup

Install Operator

## Broker Setup

```yaml
apiVersion: broker.amq.io/v2alpha5
kind: ActiveMQArtemis
metadata:
  name: artemis-cluster
spec:
  acceptors:
    - name: amqp
      port: 61616
      protocols: amqp
  addressSettings: {}
  console:
    expose: true
  deploymentPlan:
    enableMetricsPlugin: true
    extraMounts: {}
    image: placeholder
    journalType: nio
    livenessProbe: {}
    messageMigration: true
    persistenceEnabled: true
    podSecurity: {}
    readinessProbe: {}
    requireLogin: false # Update
    resources:
      limits:
        cpu: "2"
        memory: 3Gi
      requests:
        cpu: 100m
        memory: 750Mi
    size: 2
    storage:
      size: 1Gi
  upgrades:
    enabled: false
    minor: false
```

## Addresses

```yaml
apiVersion: broker.amq.io/v2alpha3
kind: ActiveMQArtemisAddress
metadata:
  name: words
spec:
  addressName: words
  queueName: words
  routingType: anycast
```

## Producer / Consumer

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: producer1
  name: producer1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: producer1
  strategy:
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: producer1
    spec:
      containers:
        - env:
            - name: QUARKUS_LOG_LEVEL
              value: DEBUG
            - name: AMQP_HOST
              value: artemis-cluster-hdls-svc
            - name: AMQP_PORT
              value: "61616"
            - name: AMQP_USERNAME
              valueFrom:
                secretKeyRef:
                  key: AMQ_CLUSTER_USER
                  name: artemis-cluster-credentials-secret
            - name: AMQP_PASSSWORD
              valueFrom:
                secretKeyRef:
                  key: AMQ_CLUSTER_PASSWORD
                  name: artemis-cluster-credentials-secret
          image: docker.io/g1raffi/amqp-producer:latest
          imagePullPolicy: Always
          name: amqp-producer
          resources: {}
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      restartPolicy: Always
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: consumer1
  name: consumer1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: consumer1
  strategy:
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: consumer1
    spec:
      containers:
        - env:
            - name: QUARKUS_LOG_LEVEL
              value: DEBUG
            - name: AMQP_HOST
              value: artemis-cluster-hdls-svc
            - name: AMQP_PORT
              value: "61616"
            - name: AMQP_USERNAME
              valueFrom:
                secretKeyRef:
                  key: AMQ_CLUSTER_USER
                  name: artemis-cluster-credentials-secret
            - name: AMQP_PASSSWORD
              valueFrom:
                secretKeyRef:
                  key: AMQ_CLUSTER_PASSWORD
                  name: artemis-cluster-credentials-secret
          image: docker.io/g1raffi/amqp-consumer:latest
          imagePullPolicy: Always
          name: amqp-consumer
          resources: {}
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      restartPolicy: Always
```
