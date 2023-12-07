# Fulcio

Install fulcio in OpenShift environment with the sigstore [fulcio](https://artifacthub.io/packages/helm/sigstore/fulcio) helm chart.

## Configuration

```yaml
namespace:
  create: false
  name: user-rhe-fulcio-test

config:
  contents: {}

# Configure ctlog dependency
ctlog:
  enabled: true
  name: ctlog
  forceNamespace: user-rhe-fulcio-test
  fullnameOverride: ctlog
  namespace:
    name: user-rhe-fulcio-test
    create: false
  createtree:
    enabled: false
  createcerts:
    name: ctlog-createcerts
    fullnameOverride: ctlog-createcerts
  createctconfig:
    logPrefix: fulcio
    fulcioURL: "http://fulcio-server.user-rhe-fulcio-test.svc.cluster.local:8080"
    securityContext:
      runAsNonRoot: true
      runAsUser: 1001800000
  server:
    config:
      key: treeID
      treeID: "4680223729387594789"
    securityContext:
      runAsNonRoot: true
      runAsUser: 1001800000
  trillian:
    namespace: user-rhe-rekor-test

server:
  svcPort: 8080
  serviceAccount:
    create: true
  service:
    type: ClusterIP
    ports:
      - name: http
        port: 8080
        protocol: TCP
        targetPort: 5555
  ingress:
    http:
      enabled: true
      hosts:
      - path: /
        host: "fulcio.apps.cloud"
  securityContext:
    runAsNonRoot: true
    runAsUser: 1001800000

createcerts:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1001800000

# Force namespace of namespaced resources
forceNamespace: user-rhe-fulcio-test

```


create signing request: cosign sign --key=k8s://user-rhe-fulcio-test/key --identity-token $SA_TOKEN --rekor-url=https://rekor.apps.cp.rch.cloud --fulcio-url=https://fulcio.apps.cp.rch.cloud/ artifactory.rch.cloud:443/devsecops-docker/buildah-test:latest -d
