# Todoapp – base SSR + PSQL Schema-Per-Tenant

A small, complete example of classic SSR with tenant isolation in PostgreSQL.

## Running from the terminal

Run all commands from the repository root. 
Docker Compose receives values from the committed `.env` file.
Application automatically loads `PSQL_CONNECTION_STRING` from it.

```sh
# Start PostgreSQL in the background
docker compose --env-file .env -f src/main/docker/docker-compose.yml up -d

# Compile and start the application
mvn compile exec:java
```

The app is available at `http://localhost:7070`.

```sh
# Stop the PostgreSQL container
docker compose --env-file .env -f src/main/docker/docker-compose.yml down

# Stop the container and remove PostgreSQL data
docker compose --env-file .env -f src/main/docker/docker-compose.yml down -v
```

The system environment variable `PSQL_CONNECTION_STRING` takes precedence over the value from `.env`.

## Running from IntelliJ IDEA

Two shared run configurations are available:

- `postgres - Docker Compose` starts the PostgreSQL service.
- `todoapp schema per tenant - PostgreSQL` compiles and starts the application.

## Demo accounts

Authentication is intentionally kept in memory.

| Account | Password | Tenant / PostgreSQL schema |
| --- | --- | --- |
| `kamil@signaturky.com` | `asdf1234` | `tenant_1` |
| `paulina@codeer.net` | `asdf1234` |  `tenant_2` |
| `lusia@codeer.net` | `asdf1234` | `tenant_2` |


## How tenant isolation works

After sign-in, `tenantId` is stored in the session.
The repository derives the schema name `tenant_<id>` from it.
On the first use of a tenant, Flyway creates its schema and runs migrations from `src/main/resources/db/migration`.

