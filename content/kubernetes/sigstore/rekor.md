# Rekor

Setup a rekor instance to create a transparency log for signing artifacts transparently.

First we set up rekor and trillian with helm, with the following values:

```yaml
namespace:
  create: false
  name: rekor-namespace
server:
  config:
    treeID: ""
  ingress:
    hosts:
    - path: /
      host: HOSTNAME
    hostname: HOSTNAME
  securityContext:
    runAsUser: 1001800000
  service:
    ports:
      - name: 3000-tcp
        port: 8080
        protocol: TCP
        targetPort: 3000
trillian:
  forceNamespace: rekor-namespace
  namespace:
    create: false
    name: rekor-namespace
```

Install the helm chart:

```bash
helm repo add sigstore https://sigstore.github.io/helm-charts
helm repo upgrade
helm upgrade -i -n rekor-namespace rekor sigstore/rekor -f values.yaml
```

TreeID is empty and rekor-server will not become stable until the merkle-tree is initialized by trillian.

Therefore install the go binary to initialize the merkle-tree:

```bash
go install github.com/google/trillian/cmd/createtree@latest
```

Then port-forward the grpc port 8091 to your local machine:

```bash
oc get pods -l app.kubernetes.io/component=server,app.kubernetes.io/instance=rekor
oc port-forward $POD_NAME 8091:8091
```

And create the merkle-tree with your go binary:

```bash
TREEID=$($GOPATH/bin/createtree --admin_server localhost:8091 | grep Initialising | sed -rE 's/.*Initialising Log ([0-9]*).*/\1/')
```

Update the helm deployment with the generated `$TREEID`

```yaml
# [...]
server:
  config:
    treeID: "$TREEID"
# [...]
```

Upgrade the helm deployment and verify the with the rekor api:

```bash
curl curl https://$REKOR_API/api/v1/log
```

```
cosign sign --key=k8s://user-rhe-fulcio-test/key --rekor-url=$REKOR_URL --registry-username=$USERNAME --registry-password=$TOKEN $REGISTRY/node-example:255202 -d
cosign verify --key=k8s://user-rhe-fulcio-test/key --insecure-ignore-tlog $REGISTRY/node-example:255202
cat cosign.verify.json | jq -r '.[1].optional.Bundle.Payload.body' | base64 -d | jq -r '.spec.signature.publicKey.content' | base64 -d
```