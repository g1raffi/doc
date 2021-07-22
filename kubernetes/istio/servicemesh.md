# Service Mesh Operator (Istio)

## Installing Operator

* Install ElasticSearch Operator
* Install Jaeger Operator
* Install Service Mesh Operator
* Install Kiali Operator


### Setup the control plane

**UI Method**: 

Create new ServiceMeshControlPlane:

* Create namespace (e.g. istio-system)
* Openshift Web Console
* Installed Operators
* Openshift Service Mesh
* Istio ServiceMeshControlPlane
* Create new Control Plane


**Declarative Method**:

Create file for ServiceMeshControlPlane (smcp):

```yaml
apiVersion: maistra.io/v2
kind: ServiceMeshControlPlane
metadata:
  finalizers:
  - maistra.io/istio-operator
  generation: 1
  name: basic
  namespace: istio-system
spec:
    grafana:
      enabled: true
    jaeger:
      install:
        storage:
          type: Memory
    kiali:
      enabled: true
    prometheus:
      enabled: true
  policy:
    type: Istiod
  profiles:
  - default
  telemetry:
    type: Istiod
  tracing:
    sampling: 10000
    type: Jaeger
  version: v2.0
```


### Create the ServiceMeshMemberRoll

Create ServiceMeshMemberRoll (smmr):

Only projects registered per ServiceMeshMemberRoll will get included in the contorl plane. There must be a ServiceMeshMemberRoll named `default` in the project of the ServiceMeshControlPlane.

```yaml

apiVersion: maistra.io/v1
kind: ServiceMeshMemberRoll
metadata:
  name: default
  namespace: istio-system
spec:
  members:
# a list of projects joined into the service mesh
#     - your-project-name
#     - another-project-name

Update and add the projects to the ServiceMeshMemberRoll and apply the resource.
```


### Opt in deployments into control plane

Workload must opt-in to be tracked by the control plane. This happens with the annotation: "sidecar.istio.io/inject":"true". Patch the desired resources to add the annotation to the workload that should be tracked.

```s
oc patch deployment DEPLOYMENTNAME -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}}}' --type=merge
```

To remove services from the service mesh delete the namespace from the ServiceMeshMemberRoll:

```s
$ oc -n istio-system patch --type='json' smmr default -p '[{"op": "remove", "path": "/spec/members", "value":["'"NAMESPACE_TO_REMOVE"'"]}]'
```

To add services to the service mesh add the namespace to the ServiceMeshMemberRoll:

```s
oc -n istio-system patch --type='json' smmr default -p '[{"op": "add", "path": "/spec/members", "value":["'"NAMESPACE_TO_ADD"'"]}]'
```

