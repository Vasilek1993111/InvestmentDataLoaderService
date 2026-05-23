# AGENTS.md

## Cursor Cloud specific instructions

### Overview

This is a multi-repo workspace with 4 Spring Boot microservices for trading/investment data from the T-Invest (Tinkoff) API:

| Service | Port | Repo Directory |
|---------|------|----------------|
| InvestmentDataLoaderService | 8087 | `/agent/repos/InvestmentDataLoaderService` |
| investmentDataScannerService | 8085 | `/agent/repos/investmentDataScannerService` |
| InvestmentDataStreamService | 8084 | `/agent/repos/InvestmentDataStreamService` |
| InvestmentTradingService | 8080 | `/agent/repos/InvestmentTradingService` |

### Prerequisites

- **Java 21** (pre-installed in the VM)
- **Maven 3.8+** (installed via `sudo apt-get install -y maven`)
- **PostgreSQL 16** on port **5434** (not the default 5432!)

### Starting PostgreSQL

```bash
sudo pg_ctlcluster 16 main start
```

Password for `postgres` user: `postgres`. The `invest` schema exists in the default `postgres` database.

### Running Services

All services use `mvn spring-boot:run -Dmaven.test.skip=true`. Key environment variables:

```bash
export SPRING_DATASOURCE_TEST_PASSWORD=postgres
export T_INVEST_TEST_TOKEN=<your-token>       # Required for actual API calls
export DB_PASSWORD=postgres                    # Used by Scanner/Stream services
```

**InvestmentDataLoaderService** (candles, instruments, dividends):
```bash
cd /agent/repos/InvestmentDataLoaderService
mvn spring-boot:run -Dspring-boot.run.profiles=test -Dmaven.test.skip=true
```

**investmentDataScannerService** (real-time market data):
```bash
cd /agent/repos/investmentDataScannerService
mvn spring-boot:run -Dmaven.test.skip=true \
  -Dspring-boot.run.arguments="--startup.price-loader.enabled=false --startup.instrument-loader.enabled=false --server.port=8085"
```

### Running Tests

```bash
# InvestmentDataStreamService (H2 in-memory, no external deps)
cd /agent/repos/InvestmentDataStreamService && mvn test

# investmentDataScannerService (H2 in-memory, no external deps)
cd /agent/repos/investmentDataScannerService && mvn test -Dspring.profiles.active=test

# InvestmentTradingService (H2 in-memory, no external deps)
cd /agent/repos/InvestmentTradingService && mvn test
```

### Known Issues

- **InvestmentDataLoaderService tests**: Some test files have stale method references (`saveShares`, `saveFutures`, `saveIndicatives`) that don't match the current service API. Test compilation fails.
- **InvestmentTradingService contextLoads test**: Fails because H2 test URL doesn't include schema init (`INIT=CREATE SCHEMA IF NOT EXISTS invest`). This is a pre-existing configuration gap.
- **Scanner startup loaders**: When starting without a valid T-Invest token, disable startup loaders via `--startup.price-loader.enabled=false --startup.instrument-loader.enabled=false`.

### Key Gotchas

- PostgreSQL runs on port **5434** (not 5432). All service configs expect this.
- The `-Dmaven.test.skip=true` flag is required for `spring-boot:run` in InvestmentDataLoaderService because test code has compilation errors.
- Services require `T_INVEST_TEST_TOKEN` (or `TINVEST_API_TOKEN` for TradingService) to make actual T-Invest API calls for market data.
- The `invest` schema must exist in PostgreSQL before services can write data.
