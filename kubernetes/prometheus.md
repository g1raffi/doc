# Prometheus

## Get CPU and Memory usage of kubernetes pods

### Get effecitve cpu usage of pod
```prometheus
rate(conatiner_cpu_usage_seconds_total{container_name != "", namespace
= ""}[5m])
```

### Cpu requests
```
kube_pod_container_resource_requests_cpu_cores{namespace = ""}
```

### Effective memory usage
```
sum_by(pod_name, instance)(container_memory_working_set_bytes{namespace=""}
/ 1024^3)
```

### Memory request
```
kube_pod_container_resource_requests_memory_bytes{namespace = ""}
```

### Memory usage relative to request

```
sum by (container, pod, namespace) (container_memory_working_set_bytes{namespace = "pitc-rhe-serverless",container!="POD",container!=""})
/
sum by (container, pod, namespace) (kube_pod_container_resource_requests_memory_bytes{namespace = "pitc-rhe-serverless"})
```

## Prometheus Rules

Create alerting rules in Prometheus:

```yaml

---
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: prometheus-custom-rules
  namespace: pitc-infra-monitoring
spec:
  groups:
    - name: custom_node_monitoring
      rules:
        - alert: kubePersistentvolumeclaimStatus
          expr: |
            kube_persistentvolumeclaim_status_phase{phase!="Bound"}
            * on (namespace) group_left() (kube_namespace_labels{label_kubernetes_io_metadata_name!="",label_pitc_sla="prod"})
            == 1
          for: 24h
          annotations:
            message: "{{$labels.namespace}}: {{$labels.persistentvolumeclaim}} has phase {{$labels.phase}}"
          labels:
            severity: warning
        - alert: kubePersistentVolumeUnused
          expr: |
            max by(provider,platform,namespace,persistentvolumeclaim)
              (
                kube_persistentvolumeclaim_status_phase{platform!="openshift3"}
              )
            * on(namespace) group_left()
              (
                kube_namespace_labels{label_pitc_customer!="fringebenefit",label_pitc_ignore_unused_volume!="true"}
              )
            unless on(persistentvolumeclaim, namespace)
            count by(provider,platform,namespace,persistentvolumeclaim, instance, node, pod)
              (
                avg_over_time(kube_pod_spec_volumes_persistentvolumeclaims_info[30d])
              )
          for: 24h
          annotations:
            message: "The PVC {{$labels.persistentvolumeclaim}} in {{$labels.namespace}} was not used for over 30 days"
          labels:
            severity: warning

```
