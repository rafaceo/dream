# cards + payment

Two Spring Boot services for managing bank cards and transferring money between them.

Stack: Java 21, Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, Liquibase, Kafka, springdoc (Swagger UI), Lombok.

## How it works

```
client - login - cards  (POST /api/auth/login - JWT)
client - JWT - payment POST /api/transfers, checks: do the cards exist, are they ACTIVE, is there enough money? If everything matches, saves Transfer(SUCCESS) + an outbox record
OutboxRelay - Kafka: transfer.completed - cards - debits/credits the balances (idempotently); on rejection cards publishes transfer.failed, the transfer status becomes FAILED - transfer.failed - payment
```

- **Transactional outbox.** payment does not write to Kafka directly: the event is saved to the `outbox_events` table together with the transfer, and `OutboxRelay` sends it to Kafka every 500 ms. If Kafka is unavailable, events are not lost and are delivered later.
- **Idempotency.** cards records processed transfers in `processed_transfers`, so a redelivered event does not change the balance again.
- **Locking.** When a transfer is applied, both cards are locked (`SELECT ... FOR UPDATE`), so concurrent transfers cannot corrupt a balance.
- **Eventual consistency.** A transfer in the payment response may be `SUCCESS` and later become `FAILED` if cards rejects it when applying it (for example, the balance changed in the meantime). Re-read the status via `GET /api/transfers/{id}`.

## Quick start

### One command (Docker only)

```bash
docker compose up --build
```

Run from the project root. Compose builds both services and starts two databases, Kafka, cards and payment. Liquibase migrations are applied automatically, and services start in the right order once the databases and Kafka are ready. The first build takes a few minutes (Maven dependencies are downloaded).

```bash
docker compose up -d --build   # in the background
docker compose logs -f cards payment
docker compose down            # stop (data is kept)
docker compose down -v         # stop and delete the database data
```

Default ports: cards `8080`, payment `8081`, Postgres `5432` and `5434`, Kafka `9092`. If one is taken, override it with environment variables or a `.env` file: `CARDS_PORT`, `PAYMENT_PORT`, `CARDS_DB_PORT`, `PAYMENT_DB_PORT`, `KAFKA_PORT`. Secrets and credentials are configured the same way (see [Configuration](#configuration)).

### Development mode (services from an IDE or Maven)

Requires JDK 21 and Docker for the infrastructure. The service folders keep their own compose files with infrastructure only. Do not run them together with the root one: they use the same ports.

```bash
# Postgres for cards
cd cards && docker compose up -d

# Postgres for payment + Kafka (shared by both services)
cd ../payment && docker compose up -d

cd ../cards   && ./mvnw spring-boot:run      # http://localhost:8080
cd ../payment && ./mvnw spring-boot:run      # http://localhost:8081
```

On Windows use `mvnw.cmd`.

### Swagger UI

- cards: http://localhost:8080/swagger-ui.html
- payment: http://localhost:8081/swagger-ui.html

Get a token via `POST /api/auth/login` in cards, click **Authorize** and paste the token (without the word `Bearer`). The token works for both services.

## Authentication

- `POST /api/auth/login` (cards, no token required) accepts `{"username","password"}` and returns `{"accessToken","tokenType":"Bearer","expiresIn":3600}`.
- Token: JWT HS256, valid for **1 hour**, `sub` holds the username.
- All other endpoints of both services require `Authorization: Bearer <token>`. A missing, invalid or expired token gives `401`. Only `/api/auth/login` and Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) are open.
- cards signs the tokens and payment only verifies them, so **the secret must be the same in both services**.
- When payment calls cards, it forwards the caller's token.
- There is only one user for now, taken from the configuration: `admin` / `admin`. This is a development placeholder.

## Examples

```bash
# token
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}' | jq -r .accessToken)
AUTH="Authorization: Bearer $TOKEN"
OWNER=$(uuidgen)

# two cards
curl -s localhost:8080/api/cards -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"cardNumber\":\"4111111111111111\",\"balance\":1000.00,\"currency\":\"USD\",\"ownerId\":\"$OWNER\",\"status\":\"ACTIVE\"}"
curl -s localhost:8080/api/cards -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"cardNumber\":\"5555555555554444\",\"balance\":0,\"currency\":\"USD\",\"ownerId\":\"$OWNER\",\"status\":\"ACTIVE\"}"

# transfer (take the card ids from the responses above)
curl -s localhost:8081/api/transfers -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"fromCardId":"<id1>","toCardId":"<id2>","amount":100.50}'

# transfer history of a card
curl -s "localhost:8081/api/transfers?cardId=<id1>&status=SUCCESS&page=0&size=20" -H "$AUTH"
```

## API

### cards (8080)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Issue a JWT (no token required) |
| POST | `/api/cards` | Add a card; the card network is detected from the number. `201` / `400` / `409` |
| GET | `/api/cards/{cardId}` | Get a card by id. `404` if not found |
| GET | `/api/cards/owner/{ownerId}` | All cards of an owner |
| PUT | `/api/cards/{cardId}` | Change the status (the only mutable field) |
| DELETE | `/api/cards/{cardId}` | Delete a card |

Card fields: `cardNumber` (13-19 digits, masked in responses), `balance` (≥ 0, up to 2 decimal places), `currency` (`USD`, `KZT`, `RUB`), `ownerId`, `status` (`ACTIVE`, `FROZEN`, `BLOCKED`), `type` (`DEBIT`, `CREDIT`). The card number is stored as an HMAC hash for duplicate detection. Unknown fields in the request body are rejected with `400`.

### payment (8081)

| Method | Path | Description |
|---|---|---|
| POST | `/api/transfers` | Create a transfer |
| GET | `/api/transfers/{transferId}` | Get a transfer by id |
| GET | `/api/transfers` | History, newest first. Filters `cardId`, `status`; `page` (zero-based), `size` (1-100) |

Response codes of `POST /api/transfers`:

| Code | When |
|---|---|
| 200 | Transfer accepted (`status: SUCCESS`) |
| 400 | Invalid body, or `fromCardId` equals `toCardId` |
| 404 | Source or destination card not found |
| 422 | A card is not `ACTIVE`, the currencies differ, or insufficient funds |
| 503 | cards is unavailable |

The reason for a rejection is in the `failureReason` field. Rejected transfers are also saved, with status `FAILED`.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `APP_JWT_SECRET` | `dev-only-change-me-…` | JWT signing secret (HS256, **at least 32 bytes**), the same in both services |
| `APP_AUTH_USERNAME` | `admin` | Login (cards only) |
| `APP_AUTH_PASSWORD` | `admin` | Password (cards only) |
| `APP_CARD_NUMBER_HASH_SECRET` | `dev-only-change-me` | Secret for hashing card numbers (cards) |

Everything else (ports, database and Kafka addresses, `cards.base-url`, timeouts, topics) is in each service's `src/main/resources/application.properties`. Topics: `transfer.completed` and `transfer.failed`.

> The defaults are for development only. Change all secrets and credentials before any real deployment.

## Project structure

```
dream/
├── docker-compose.yml   the whole system with one command
├── cards/     Dockerfile, controller → service → repository, event (Kafka), config (Security, OpenAPI), db/changelog
└── payment/   Dockerfile, controller → service → repository, client (cards), outbox, event (Kafka), config, db/changelog
```

## Tests and build

```bash
./mvnw test      # in each service
./mvnw package
```

`contextLoads` starts the whole application, so it needs Postgres and Kafka to be running.
