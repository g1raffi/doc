# OpenShift

## Go Templates

Get route host

```s
curl $(oc get route ROUTE_NAME -o go-template='{{(index .status.ingress 0).host}}')/data
```

Compare ready and desired pod count

```s
[[ $(oc -n NAMESPACE get dc DC_NAME -o go-template='{{.status.readyReplicas}}') == $(oc -n NAMESPACE get dc DC_NAME -o go-template='{{.status.replicas}}') ]]
```


## Simple automated apply environment script

```bash
#!/bin/bash

echo "You are currently on project:"
echo "-------------"
oc status | head -1
echo "-------------"

echo "Are you sure you want to apply all resources for environment '$1'?"
echo "Press <Enter> to do so. Or press <Ctrl>+<C> to abort"
read DUMMY

ENVIRONMENT=$1
SECRETS="artemis-credentials db-credentials jwt-tokens
postgres-credentials"
INFRASTRUCTURE="routes artemis db-backup waf
service-monitor-artemis-activemq service-monitor-postgresql
service-monitor-spring-boot"
APPS="application"
STATIC=""

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"

if [ -z "$ENVIRONMENT" ]; then
  echo 'Usage: ./apply-env.sh ENVIRONMENT'
  exit 1
fi

if [ ! -z "$2" ]; then
  APPS=$2
fi

## configuration script for the environment ##

# apply all secrets
echo 'Applying all secrets and configurations'
for secret in ${SECRETS}; do
  if [ -f "${DIR}/../template/secret-${secret}.yml" ]; then
      oc get secret ${secret} ||
      oc process -f ${DIR}/../template/secret-${secret}.yml --param-file \
      ${DIR}/../environments/env.yml --ignore-unknown-parameters \
    | oc apply -f -
  fi
done

echo 'Applying all infrastructure resources'
for infra in ${INFRASTRUCTURE}; do
  if [ -f "${DIR}/../template/infra-${infra}.yml" ]; then
      oc process -f ${DIR}/../template/infra-${infra}.yml --param-file \
      ${DIR}/../environments/env.yml --ignore-unknown-parameters \
    | oc apply -f -
  fi
done

echo 'Applying all application resources'
for app in ${APPS}; do
  if [ -f "${DIR}/../template/app-${app}.yml" ]; then
      oc process -f ${DIR}/../template/app-${app}.yml --param-file \
      ${DIR}/../environments/env.yml --ignore-unknown-parameters \
    | oc apply -f -
  fi
done

echo 'Applying all static resources'
for stat in ${STATIC}; do
  if [ -f "${DIR}/../static/${stat}.yml" ]; then
      oc apply -f ${DIR}/../static/${stat}.yml
  fi
done

echo 'Done!'
```
