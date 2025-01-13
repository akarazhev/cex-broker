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
podman build -t cex-broker:1.0 . 
podman run -p 8080:8080 cex-broker:1.0
```