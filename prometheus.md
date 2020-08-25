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

