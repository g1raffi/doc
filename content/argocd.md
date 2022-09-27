# ArgoCD

## Create application argocd

apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: rhe-serverless
  namespace: argocd
spec:
  destination:
    namespace: ${NAMESPACE}
    server: # Clusterroute or Clustername e.g.: https://${CLUSTER_API}
  project: project_name
  source:
    path: src/main/openshift
    repoURL: https://${TOKEN_NAME}:${TOKEN_VALUE}@gitlab.puzzle.ch/some-repo.git
    - CreateNamespace=true

## Ignore certain properties

When using ArgoCD with HPA or Autoscaled Resources (keda.sh), you can ignore certain fields like this:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
spec:
  # [...]
  ignoreDifferences:
  - group: "apps"
    kind: "Deployment"
    jsonPointers:
    - /spec/replicas

  syncPolicy:
    syncOptions:
    - RespectIgnoreDifferences=true
```
