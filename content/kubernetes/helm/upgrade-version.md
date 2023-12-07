# Upgrade version

## yq

```bash
chart="example-chart" newver="1.0.0" yq -i '(.dependencies[] | select (.name == strenv(chart))).version = strenv(newver)' Chart.yaml
```
