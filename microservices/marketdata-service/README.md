# Market Data Service

Reactive Spring Boot 4.0.0 microservice that ingests quotes from free APIs, writes historical data to MongoDB, and publishes canonical price events to Kafka.

## Features
- WebFlux-based ingestion with circuit breaker via Resilience4j
- Reactive MongoDB persistence for historical candles
- Kafka producer publishing to `prices.{symbol}`
- Actuator endpoints for health/metrics (Prometheus-ready)
- Integration test using Testcontainers (Kafka + MongoDB + WireMock)

## Running Locally
```bash
./mvnw test
./mvnw spring-boot:run
```

Environment variables (override defaults in `application.yml`):
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `SPRING_DATA_MONGODB_URI`
- `MARKETDATA_UPSTREAM_BASE_URL`
- `MARKETDATA_KAFKA_TOPIC_PATTERN`

## Endpoints
- `POST /marketdata/symbols` – ingest symbol immediately
- `GET /marketdata/{symbol}/quote` – fetch + publish latest quote
- `GET /marketdata/{symbol}/history?start=2024-01-01T00:00:00Z&end=2024-01-02T00:00:00Z` – read persisted candles

## Kafka Schema (JSON)
```json
{
  "symbol": "AAPL",
  "timestamp": "2024-01-01T00:00:00Z",
  "last": 191.22,
  "bid": 191.20,
  "ask": 191.25,
  "volume": 1200000
}
```

## Docker
```
./mvnw -DskipTests package
docker build -t stock-metrics/marketdata-service:local .
```
