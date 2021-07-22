# ArgoCD

## Create application argocd

apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: pitc-rhe-serverless
  namespace: pitc-infra-argocd
spec:
  destination:
    namespace: ${NAMESPACE}
    server: # Clusterroute or Clustername e.g.: https://api.ocp-staging.cloudscale.puzzle.ch:6443
  project: pitc-apps
  source:
    path: src/main/openshift
    repoURL: https://${TOKEN_NAME}:${TOKEN_VALUE}@gitlab.puzzle.ch/some-repo.git
    - CreateNamespace=true
