# Windoof

CP File from pod to windows in gitbash

```shell
oc exec PODNAME -c CONTAINER_NAME -- bash -c "base64 FILE" | base64 -d > localfile
```
