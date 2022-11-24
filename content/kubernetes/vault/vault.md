# Vault

## Extract Kubernets Secrets and POST to Vault

```bash
#!/bin/bash
 
echo "--------------------------------------------------------------"
echo "OpenShift Vault Importer"
echo "--------------------------------------------------------------"
 
read -p "Enter vault address [default: \$VAULT_ADDRESS]: " addr
addr=${addr:-$VAULT_ADDRESS}
 
read -sp "Enter vault token [default: \$VAULT_TOKEN]: " token
token=${token:-$VAULT_TOKEN}
echo ""
 
read -p "Enter Vault Namespace [default: VAULT NAMESPACE]: " vns
vns=${vns:-VAULT NAMESPACE}
 
read -p "Enter OpenShift Namespace: " namespace
 
read -p "Enter Secret name: " secret
 
echo '{"data":' > request.json
oc get -n $namespace secret $secret -ojsonpath='{.data}' >> request.json
echo '}' >> request.json
 
echo "$(cat request.json)"
read -p "Want to apply this secret? [CTRL-C to abort, ENTER to continue] " yes
 
curl -vk \
    -H "X-Vault-Token: $token" \
    -H "X-Vault-Namespace: $vns" \
    -H "Content-Type: application/json" \
    -X POST \
    -d @request.json \
    "$addr/v1/general-secrets/data/$secret"
 
rm request.json
```
