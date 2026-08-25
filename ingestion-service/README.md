# IngestionServiceApp

## Overview

Parses and cleans `hubs-global.csv`, a messy legacy export containing hub and sorting center data.

The Ingestion Service is the first service in the LogisticsConnect pipeline. It cleans and normalizes the raw CSV data before exposing the cleaned records through a REST API for other services to consume.

Part of the [LogisticsConnect](../README.md) project.

## Data Cleaning

The service handles the following issues in `hubs-global.csv`:

- **Inconsistent casing**
  - Hub IDs are normalized to uppercase.
  - Province names and sorting center names are normalized to consistent casing.

- **Whitespace and padding**
  - Leading and trailing whitespace is removed.
  - Multiple internal spaces are collapsed into a single space.

- **Boolean normalization**
  - `Y`, `yes`, `1`, and `true` are normalized to `true`.
  - `N`, `no`, `0`, and `false` are normalized to `false`.

- **Missing and placeholder values**
  - Values such as blank fields, `N/A`, and `unknown` are handled as missing values.

- **Duplicate records**
  - Duplicate hubs are detected using the normalized province and sorting center.
  - The service keeps one cleaned record for each real-world hub/sorting center combination.

- **Naming variations**
  - Province naming variations such as `Kwa-Zulu Natal` and `KwaZulu Natal` are normalized to `KwaZulu-Natal`.

## Example

### Raw data

```text
H-502 ,gauteng,Pretoria North,0
H-505,Western Cape ,Cape Town  Port,TRUE
```

### Cleaned data

| Hub ID | Province | Sorting Center | Active |
|---|---|---|---|
| H-502 | Gauteng | Pretoria North | false |
| H-505 | Western Cape | Cape Town Port | true |

## REST Endpoints

### Health Check

```text
GET /health
```

Example:

```bash
curl http://localhost:7050/health
```

Response:

```text
OK
```

### Get Cleaned Hub Records

```text
GET /hubs
```

Example:

```bash
curl http://localhost:7050/hubs
```

Example response:

```json
[
  {
    "hubId": "H-500",
    "province": "Gauteng",
    "sortingCenter": "Johannesburg Central",
    "active": true
  },
  {
    "hubId": "H-501",
    "province": "Western Cape",
    "sortingCenter": "Cape Town Port",
    "active": true
  },
  {
    "hubId": "H-502",
    "province": "Gauteng",
    "sortingCenter": "Pretoria North",
    "active": false
  }
]
```

## Project Structure

```text
ingestion-service/
├── pom.xml
├── README.md
└── src/main/
    ├── java/
    │   └── co/wethinkcode/logisticsconnect/
    │       ├── IngestionServiceApp.java
    │       └── Hub.java
    └── resources/
        └── hubs-global.csv
```

## Build

Run the following command inside the `ingestion-service` directory:

```bash
mvn clean package
```

A successful build creates:

```text
target/ingestion-service.jar
```

## Run

Start the service with:

```bash
java -jar target/ingestion-service.jar
```

The service listens on port:

```text
7050
```

## Manual Testing

Check that the service is running:

```bash
curl http://localhost:7050/health
```

Expected response:

```text
OK
```

Retrieve the cleaned hub data:

```bash
curl http://localhost:7050/hubs
```

The response should be a JSON array containing cleaned and normalized hub records.

## Stage 1 Completion

Stage 1 implements the following:

- [x] Reads `hubs-global.csv`
- [x] Normalizes casing
- [x] Removes unnecessary whitespace
- [x] Collapses internal double spaces
- [x] Normalizes boolean values
- [x] Handles missing and placeholder values
- [x] Detects and resolves duplicate records
- [x] Normalizes naming variations
- [x] Exposes cleaned records through a REST endpoint
- [x] Provides a `/health` endpoint

## Next Stage

The cleaned hub data produced by this service will be consumed by the `hub-service` during Stage 2 of the LogisticsConnect project.