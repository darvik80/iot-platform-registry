Local Development Environment
=======================

Run docker compose to set up local development environment. Current supports following middlewares.

| Middleware     | Version | Status             |
|----------------|---------|--------------------|
| __RabbitMQ__   | latest  | :heavy_check_mark: |
| __Postgres__   |         |                    |
| __Prometheus__ |         |                    |
| __Grafana__    |         |                    |

Middlewares
===========

Rabbitmq
--------

Enable plugins

* rabbitmq_prometheus
* rabbitmq_web_mqtt
* rabbitmq_mqtt
* rabbitmq_management
* rabbitmq_event_exchange
* rabbitmq_message_timestamp (Enable if the RabbitMQ version < 3.12. More details https://github.com/rabbitmq/rabbitmq-message-timestamp)

[Generate self-sign certificates for enabling TLS][5]

> It is possible to reuse a single certificate/key pair for all nodes and CLI tools.
> The certificate can also use a wildcard Subject Alternative Name (SAN) or Common Name (CN) such as *.rabbitmq.example.local that would match every hostname in the cluster.
> 
> Replace `bifrost.hostname` by real bifrost hostname e.g: `*.laz.ipa`

````bash
# generate self-signed certificates
cd tls-gen/basic
# generate certs with custom CN
make PASSWORD=<your_password_here> CN=\*.laz.ipa
# use "host-neutral" names such as client_certificate.pem and client_key.pem
make alias-leaf-artifacts CN=\*.laz.ipa
# verify certs
make verify CN=stpk-bfs-03004.laz.ipa
# display certs info
make info CN=stpk-bfs-03004.laz.ipa
# view certs
ls -l ./result

cd ./result
# remove password in client_key.pem for GO mqtt client to load
openssl rsa -in client_key.pem -out client_key_unencrypted.pem -passin pass:<your_password_here>
````

Test SSL
```bash
./testssl.sh localhost:5671
```

Generate additional client certificate
```bash
make CN=<new_name> gen-client
# run it and ignore for an error due to missing server cert of new CN
make alias-leaf-artifacts CN=<new_name>
openssl rsa -in client_key.pem -out client_key_unencrypted.pem -passin pass:<your_password_here>
```

Postgres
--------

Prometheus
----------

Connections
===========

| Service        | Exposed Port |
|----------------|--------------|
| __AMQP/TLS__   | `5671`       |
| __Management__ | `15672`      |
| __MQTT/TLS__   | `8883`       |
| __WebSocket__  | `15675`      |


Docker Run
==========

```bash
docker compose up -d
docker compose down
```
logs: docker/gateway-logs

Build for Arm64v8/Amd64
```bash
docker compose up build_arm64v8 buil_amd64
```


Reference
=========

[1]: https://github.com/riotu-lab/rabbitmq-docker/blob/main/docker-compose.yml
[2]: https://github.com/mpolinowski/rabbitmq-mqtt-ws-docker/blob/master/docker-compose.yml
[3]: https://mpolinowski.github.io/docs/IoT-and-Machine-Learning/MQTT/2022-03-30--mqtt-with-rabbit-mq/2022-03-30/
[4]: https://github.com/settlemint/rabbitmq-mqtt/blob/master/Dockerfile
[5]: https://github.com/rabbitmq/tls-gen/tree/main/basic
[6]: https://testssl.sh/
[7]: https://github.com/mpolinowski/go-mqtt-client/blob/master/protocols/connections.go