# Подготовка переменных окружения
1. Скопировать файл api.env.template в api.env
2. Поменять переменные окружения в api.env на свои для базы данных и для rabbitmq

# Rabbit MQ генерация ключей

```bash
cd rabbitmq
git clone https://github.com/rabbitmq/tls-gen tls-gen
cd tls-gen/basic
# private key password
make PASSWORD=bunnies CN=rabbitmq
make verify
make info
ls -l ./result
cp -R ./result/ ../../cert
```

# nginx

* RabbitMQ: http://localhost:8082/rabbitmq/
<br>login/pass: ${RABBITMQ_ADMIN_USER}/RABBITMQ_ADMIN_PASSWORD}

* Grafana: http://localhost:8082/grafana/

* Prometheus: http://localhost:8082/prometheus/

* Node-Red: http://localhost:8082/node-red/
