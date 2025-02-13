# cex-broker

CEX Broker is a high-performance application designed to interact with cryptocurrency exchanges, specifically Bybit in this implementation. It uses WebSocket connections to stream real-time market data and can forward this data to a Kafka topic for further processing or analysis.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Features

- Real-time WebSocket connection to Bybit exchange
- Streaming of ticker data for specified cryptocurrency pairs
- Integration with Apache Kafka for data forwarding
- Configurable through environment variables
- Containerized for easy deployment using Podman or Docker

## Prerequisites

- Java 23 or later
- Maven 3.6 or later
- Podman or Docker (for containerized deployment)
- Access to a Kafka cluster (for data forwarding)

## Installation

1. Clone the repository:

```bash
git clone https://github.com/akarazhev/cex-broker.git
cd cex-broker
```

2. Build the project:

```bash
mvn clean package
```

This will create a JAR file with all dependencies included.

## Usage

### Running locally

To run the application locally:

```bash
java -jar target/cex-broker-0.1-SNAPSHOT-jar-with-dependencies.jar
```

### Running with Docker/Podman

1. Build the Docker image:

```bash
podman build -t cex-broker:0.1 .
```

2. Run the container:

```bash
podman run -e KAFKA_TOPIC=CEX_BROKER -e BOOTSTRAP_SERVERS=localhost:9092 -e WEB_SOCKET_TOPICS=tickers.BTCUSDT cex-broker:0.1
```

## Configuration

The application can be configured using the following environment variables:

- `KAFKA_TOPIC`: The Kafka topic to which data will be published (default: "CEX_BROKER")
- `BOOTSTRAP_SERVERS`: The Kafka bootstrap servers (default: "localhost:9092")
- `WEB_SOCKET_TOPICS`: Comma-separated list of WebSocket topics to subscribe to (default: "tickers.BTCUSDT")

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.