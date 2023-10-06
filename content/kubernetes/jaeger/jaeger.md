# Jaeger

## Deployment OpenShift / Kubernetes

Production ready deployment with self-provisioned elasticsearch:

ElasticSearch:
```yaml
---
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: quickstart
spec:
  version: 8.10.2
  nodeSets:
  - name: default
    count: 3
    podTemplate:
      spec:
        containers:
        - name: elasticsearch
          resources:
            requests:
              memory: 4Gi
              cpu: 2
            limits:
              memory: 4Gi
    volumeClaimTemplates:
      - metadata:
          name: elasticsearch-data
        spec:
          accessModes:
          - ReadWriteOnce
          storageClassName: csi-cephfs-sc
          resources:
            requests:
              storage: 5Gi
    config:
      node.store.allow_mmap: false
```

Extract secret for jaeger: 
`PASSWORD=$(kubectl get secret quickstart-es-elastic-user -o=jsonpath='{.data.elastic}' | base64 --decode)`
`oc create secret generic jaeger-secret --from-literal=ES_PASSWORD=${PASSWORD} --from-literal=ES_USERNAME=elastic`

Create jaeger instance:

```yaml
---
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: simple-prod
spec:
  strategy: production
  storage:
    type: elasticsearch
    elasticsearch:
      name: quickstart
      properties:
        doNotProvision: true
    options:
      es:
        server-urls: https://quickstart-es-http.user-rhe-jaeger-test:9200
        tls:
          ca: /es/certificates/ca.crt
        version: 7
        create-index-templates: false
    secretName: jaeger-secret
  volumeMounts:
    - name: certificates
      mountPath: /es/certificates/
      readOnly: true
  volumes:
    - name: certificates
      secret:
        secretName: quickstart-es-http-certs-public
```
