# Grafana Dashboards

TODO (Task 7): Export your Grafana dashboards as JSON and save them here.

After building your dashboards in the Grafana UI, export each one via the API:

  curl -u admin:admin123 \
    http://localhost:3000/api/dashboards/uid/<dashboard-uid> \
    | python3 -m json.tool > monitoring/grafana-dashboards/application-dashboard.json

Required files:
  - infrastructure-dashboard.json
  - application-dashboard.json

Refer to Task 7 in the README for the full export command and panel requirements.
