# Litmus

## Install Litmus namespace based

Installation [Guide](https://docs.litmuschaos.io/docs/user-guides/chaoscenter-namespace-scope-installation/)

Create scc:

```yaml
allowHostDirVolumePlugin: true
allowHostIPC: false
allowHostNetwork: false
allowHostPID: true
allowHostPorts: false
allowPrivilegeEscalation: true
allowPrivilegedContainer: true
allowedCapabilities: null
apiVersion: security.openshift.io/v1
defaultAddCapabilities:
- NET_ADMIN
- SYS_ADMIN
fsGroup:
 type: MustRunAs
kind: SecurityContextConstraints
metadata:
 name: litmus
priority: null
readOnlyRootFilesystem: false
requiredDropCapabilities:
- KILL
- MKNOD
- SETUID
- SETGID
runAsUser:
 type: RunAsAny
seLinuxContext:
 type: MustRunAs
supplementalGroups:
 type: RunAsAny
users: []
volumes:
- configMap
- downwardAPI
- emptyDir
- persistentVolumeClaim
- projected
- secret
```

Bind Service accounts to SCC:

```shell
oc adm policy add-scc-to-user litmus -z litmus-server-account
oc adm policy add-scc-to-user litmus -z chaos-mongodb 
oc adm policy add-scc-to-user litmus -z litmus-namespace-scope
oc adm policy add-scc-to-user litmus -z argo-chaos
```

Create override config when using OpenShift instead of plain Kubernetes:

```shell
cat <<EOF > override-openshift.yaml
portalScope: namespace
portal.server.service.type: ClusterIP
portal.frontend.service.type: ClusterIP
openshift.route.enabled: true
EOF
```

Install helm release:

```shell
helm install chaos litmuschaos/litmus --namespace=pitc-rhe-litmus -f override-openshift.yaml
```

Delete `securityContext` from mongodb Deployment, workflow-controller Deployment, event-tracker Deployment, subscriber Deployment, chaos-operator-ce Deployment, chaos-controller ReplicaSet and Deployment.template and frontend Deployment.template.

Create ChaosExperiment:

```yaml
kind: Workflow
apiVersion: argoproj.io/v1alpha1
metadata:
  name: kill-random-pods-1664177150
  namespace: pitc-rhe-litmus
  creationTimestamp: null
  labels:
    cluster_id: 3acb5850-bb73-4269-ac92-e7bc26d3547c
    subject: kill-random-pods_pitc-rhe-litmus
    workflow_id: 704f3fe2-b762-4b3b-9419-c1b427140ebf
    workflows.argoproj.io/controller-instanceid: 3acb5850-bb73-4269-ac92-e7bc26d3547c
spec:
  templates:
    - name: custom-chaos
      inputs: {}
      outputs: {}
      metadata: {}
      steps:
        - - name: install-chaos-experiments
            template: install-chaos-experiments
            arguments: {}
        - - name: pod-delete-fi3
            template: pod-delete-fi3
            arguments: {}
    - name: install-chaos-experiments
      inputs:
        artifacts:
          - name: pod-delete-fi3
            path: /tmp/pod-delete-fi3.yaml
            raw:
              data: >
                apiVersion: litmuschaos.io/v1alpha1

                description:
                  message: |
                    Deletes a pod belonging to a deployment/statefulset/daemonset
                kind: ChaosExperiment

                metadata:
                  name: pod-delete
                  labels:
                    name: pod-delete
                    app.kubernetes.io/part-of: litmus
                    app.kubernetes.io/component: chaosexperiment
                    app.kubernetes.io/version: 2.12.0
                spec:
                  definition:
                    scope: Namespaced
                    permissions:
                      - apiGroups:
                          - ""
                        resources:
                          - pods
                        verbs:
                          - create
                          - delete
                          - get
                          - list
                          - patch
                          - update
                          - deletecollection
                      - apiGroups:
                          - ""
                        resources:
                          - events
                        verbs:
                          - create
                          - get
                          - list
                          - patch
                          - update
                      - apiGroups:
                          - ""
                        resources:
                          - configmaps
                        verbs:
                          - get
                          - list
                      - apiGroups:
                          - ""
                        resources:
                          - pods/log
                        verbs:
                          - get
                          - list
                          - watch
                      - apiGroups:
                          - ""
                        resources:
                          - pods/exec
                        verbs:
                          - get
                          - list
                          - create
                      - apiGroups:
                          - apps
                        resources:
                          - deployments
                          - statefulsets
                          - replicasets
                          - daemonsets
                        verbs:
                          - list
                          - get
                      - apiGroups:
                          - apps.openshift.io
                        resources:
                          - deploymentconfigs
                        verbs:
                          - list
                          - get
                      - apiGroups:
                          - ""
                        resources:
                          - replicationcontrollers
                        verbs:
                          - get
                          - list
                      - apiGroups:
                          - argoproj.io
                        resources:
                          - rollouts
                        verbs:
                          - list
                          - get
                      - apiGroups:
                          - batch
                        resources:
                          - jobs
                        verbs:
                          - create
                          - list
                          - get
                          - delete
                          - deletecollection
                      - apiGroups:
                          - litmuschaos.io
                        resources:
                          - chaosengines
                          - chaosexperiments
                          - chaosresults
                        verbs:
                          - create
                          - list
                          - get
                          - patch
                          - update
                          - delete
                    image: litmuschaos/go-runner:2.12.0
                    imagePullPolicy: Always
                    args:
                      - -c
                      - ./experiments -name pod-delete
                    command:
                      - /bin/bash
                    env:
                      - name: TOTAL_CHAOS_DURATION
                        value: "15"
                      - name: RAMP_TIME
                        value: ""
                      - name: FORCE
                        value: "true"
                      - name: CHAOS_INTERVAL
                        value: "5"
                      - name: PODS_AFFECTED_PERC
                        value: ""
                      - name: LIB
                        value: litmus
                      - name: TARGET_PODS
                        value: ""
                      - name: NODE_LABEL
                        value: ""
                      - name: SEQUENCE
                        value: parallel
                    labels:
                      name: pod-delete
                      app.kubernetes.io/part-of: litmus
                      app.kubernetes.io/component: experiment-job
                      app.kubernetes.io/version: 2.12.0
      outputs: {}
      metadata: {}
      container:
        name: ""
        image: litmuschaos/k8s:2.12.0
        command:
          - sh
          - -c
        args:
          - kubectl apply -f /tmp/pod-delete-fi3.yaml -n
            {{workflow.parameters.adminModeNamespace}} &&  sleep 30
        resources: {}
    - name: pod-delete-fi3
      inputs:
        artifacts:
          - name: pod-delete-fi3
            path: /tmp/chaosengine-pod-delete-fi3.yaml
            raw:
              data: |
                apiVersion: litmuschaos.io/v1alpha1
                kind: ChaosEngine
                metadata:
                  namespace: "{{workflow.parameters.adminModeNamespace}}"
                  generateName: pod-delete-fi3
                  labels:
                    workflow_run_id: "{{workflow.uid}}"
                spec:
                  appinfo:
                    appns: pitc-rhe-litmus
                    applabel: app=example-web-go
                    appkind: deployment
                  engineState: active
                  chaosServiceAccount: litmus-admin
                  experiments:
                    - name: pod-delete
                      spec:
                        components:
                          env:
                            - name: TOTAL_CHAOS_DURATION
                              value: "30"
                            - name: CHAOS_INTERVAL
                              value: "10"
                            - name: FORCE
                              value: "false"
                            - name: PODS_AFFECTED_PERC
                              value: ""
                        probe:
                          - name: go-example
                            type: httpProbe
                            mode: Continuous
                            runProperties:
                              probeTimeout: 10
                              retry: 10
                              interval: 10
                              probePollingInterval: 10
                              initialDelaySeconds: 10
                              stopOnFailure: false
                            httpProbe/inputs:
                              url: http://example:5000
                              insecureSkipVerify: true
                              responseTimeout: 2
                              method:
                                get:
                                  criteria: ==
                                  responseCode: "200"
      outputs: {}
      metadata:
        labels:
          weight: "10"
      container:
        name: ""
        image: litmuschaos/litmus-checker:2.12.0
        args:
          - -file=/tmp/chaosengine-pod-delete-fi3.yaml
          - -saveName=/tmp/engine-name
        resources: {}
  entrypoint: custom-chaos
  arguments:
    parameters:
      - name: adminModeNamespace
        value: pitc-rhe-litmus
  serviceAccountName: argo-chaos
status:
  ? startedAt
  ? finishedAt
```
