# cex-broker

CEX Broker

## How to build

```bash
git clone https://github.com/akarazhev/cex-broker.git
cd cex-broker
mvn clean package
```

## How to run

```bash
podman build -t cex-broker:0.1 . 
podman run -e KAFKA_TOPIC=CEX_BROKER -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 -e WEB_SOCKET_TOPICS=tickers.BTCUSDT cex-broker:0.1
```