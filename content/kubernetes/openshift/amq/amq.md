# AMQ Openshift Setup

## Links

[Blog Red Hat](https://developers.redhat.com/blog/2020/01/10/architecting-messaging-solutions-with-apache-activemq-artemis#capacity_planning)

Install Operator

Check and install resources.

## Configuration

Almost all aspects of the broker on openshift are configured via the CR available. Most settings can be applied in the main resource `ActiveMQArtemis`.


### Addresses

Adresses can be configured with the `ActiveMQArtemisAddress` custom resource. An example can be found [here](activemqartemisaddress-words.yaml).


### Broker

The broker will be configured with the `ActiveMQArtemis` custom resouce. [Example](broker.yaml).


### Securit**

Users, Groups and their roles and access strategies to addresses / queues can be defined in the `ActiveMQArtemisSecurity` custom resource.


## HA Setup


### Broker

On Kubernetes, broker HA is achieved through health checks and container restarts. On-premise, the broker HA is achieved through master/slave (shared store or replication). When replication is used, the slave will already hold the queues in memory, and therefore is pretty much ready to go in case of failover. With shared storage, when the slave gets hold of the lock, then the queues need to be read from the journals ahead of the slave takeover. The time for a shared storage slave to take over will be dependent on the number and size of messages in the journal.

When we talk about broker HA, it comes down to an active-passive failover mechanism (with Kubernetes being an exception). But Artemis also has an active-active clustering mechanism used primarily for scalability rather than HA. In active-active clustering, every message belongs to only one broker, and losing an active broker will make its messages also unaccessible—but a positive side effect of that issue is that the broker infrastructure is still up and functioning. Clients can use active instances and exchange messages with the drawback of temporarily not accessing the messages that are in the failed broker. To sum up, active-active clustering is primarily for scalability, but it also partially improves the availability with temporary message unavailability.
Load balancer

If there is a load balancer, prefer one that is already HA in the organization, such as F5s. If Qpid is used, you will need two or more active instances for high availability.
Clients

This is probably the easiest part, as most customers will already run the client services in redundantly HA fashion, which means two or more instances of consumers and producers most of the time. A side effect of running multiple consumers is that message ordering is not guaranteed. This is where message groups and exclusive consumers can be used.


### Scalability

Scalability is relatively easier to achieve with Artemis. Primarily, there are two approaches to scaling the message broker.
Active-active clustering

Create a single logical broker cluster that is scaled transparently from the clients. This can be three masters and three slaves (replication or shared storage doesn’t matter) to start with, which means that clients can use any of the masters to produce and consume the messages. The broker will perform load balancing and message distributions. Such a messaging infrastructure is scalable and supports many queues and topics with different messaging patterns. Artemis can handle large and small messages effectively, so there is no need for using separate broker clusters depending on the message size either.

A few of the consequences of active-active clustering are:

    Message ordering is not preserved.
    Message grouping needs to be clustered.
    Scaling down requires message draining.
    Browsing the brokers and the queues is not centralized.

### Mutual TLS (no istia)


#### Client side

When a client tries to connect to a broker Pod in your deployment, the verifyHost option in the client connection URL determines whether the client compares the Common Name (CN) of the broker’s certificate to its host name, to verify that they match. The client performs this verification if you specify verifyHost=true or similar in the client connection URL. 


#### Broker side

Generate a self-signed certificate for the broker key store.

`$ keytool -genkey -alias broker -keyalg RSA -keystore ~/broker.ks`

Export the certificate from the broker key store, so that it can be shared with clients. Export the certificate in the Base64-encoded .pem format. For example:

`$ keytool -export -alias broker -keystore ~/broker.ks -file ~/broker_cert.pem`

On the client, create a client trust store that imports the broker certificate.

`$ keytool -import -alias broker -keystore ~/client.ts -file ~/broker_cert.pem`

On the client, generate a self-signed certificate for the client key store.

`$ keytool -genkey -alias broker -keyalg RSA -keystore ~/client.ks`

On the client, export the certificate from the client key store, so that it can be shared with the broker. Export the certificate in the Base64-encoded .pem format. For example:

`$ keytool -export -alias broker -keystore ~/client.ks -file ~/client_cert.pem`

Create a broker trust store that imports the client certificate.

`$ keytool -import -alias broker -keystore ~/broker.ts -file ~/client_cert.pem`

Log in to OpenShift Container Platform as an administrator. For example:

`$ oc login -u system:admin`

Switch to the project that contains your broker deployment. For example:

`$ oc project <my_openshift_project>`

Create a secret to store the TLS credentials. For example:

```s
    $ oc create secret generic my-tls-secret \
    --from-file=broker.ks=~/broker.ks \
    --from-file=client.ts=~/broker.ts \
    --from-literal=keyStorePassword=<password> \
    --from-literal=trustStorePassword=<password>
```

Note

When generating a secret, OpenShift requires you to specify both a key store and a trust store. The trust store key is generically named client.ts. For two-way TLS between the broker and a client, you must generate a secret that includes the broker trust store, because this holds the client certificate. Therefore, in the preceding step, the value that you specify for the client.ts key is actually the broker trust store file.

Link the secret to the service account that you created when installing the Operator. For example:

`$ oc secrets link sa/amq-broker-operator secret/my-tls-secret`

Specify the secret name in the sslSecret parameter of your secured acceptor or connector. For example:

```yaml
    spec:
    ...
      acceptors:
      - name: my-acceptor
        protocols: amqp,openwire
        port: 5672
        sslEnabled: true
        sslSecret: my-tls-secret
        expose: true
        connectionsAllowed: 5
    ...
```
