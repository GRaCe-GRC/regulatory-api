# regulatory-api

Inner API for regulatory files and requirements (Regulatory database).

> **Status:** stores regulatory authorities in the `regulatory` database (no endpoints yet).

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)
- PostgreSQL with Spring Data JDBC, schema managed by Flyway (`src/main/resources/db/migration`)

## Getting started
Start the database from [GRaCe-GRC-sandbox](https://github.com/GRaCe-GRC/GRaCe-GRC-sandbox), then the app:

```bash
(cd ../GRaCe-GRC-sandbox/compose && docker compose up -d --wait)
./mvnw spring-boot:run
```

On startup Flyway applies any new migrations. Entities are Java records mapped with Spring Data JDBC.

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |

## Database

| Environment variable | Default |
|---|---|
| `REGULATORY_DB_HOST` | `localhost` |
| `REGULATORY_DB_PORT` | `5432` |
| `REGULATORY_DB_NAME` | `regulatory` |
| `REGULATORY_DB_USERNAME` | `regulatory_api` |
| `REGULATORY_DB_PASSWORD` | `regulatory_api` |

The defaults match the sandbox and are for local use only.

| Table | Migration |
|---|---|
| `authorities` | `V1__create_authorities.sql` |

## Test and build
```bash
./mvnw verify
```

Branch verify runs the same command on every push to a branch and on every pull request, through the
shared [Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml) (`.github/workflows/branch-verify.yml`).

## Versions and images
Versions follow semantic versioning and come from the commit messages. Every merge to `main`
runs `.github/workflows/release.yml`: the CI again, then the shared
[release workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/README.md#release):

| Commit | Release |
|---|---|
| `fix: GRC-<n>: ...` | Patch |
| `feat: GRC-<n>: ...` | Minor |
| `chore:`, `docs:`, ... | No release |

A release creates the `vX.Y.Z` tag, a GitHub Release with notes, and the image
`ghcr.io/grace-grc/regulatory-api:<version>` (also tagged `latest`). The version is not stored in
`pom.xml`: it uses `${revision}`, which is `0.0.1-SNAPSHOT` for local builds and the release
version in CI.

```bash
docker pull ghcr.io/grace-grc/regulatory-api:latest
```

## Architecture
This repository is part of the **inner API layer**: services reach the system of records only through it.
Other projects use it through internal service APIs and events, referring to its entities by ID.
See the [GRaCe-GRC documentation hub](https://github.com/GRaCe-GRC/GRaCe-GRC-docs).

## Contributing
See the organization's [contributing guide](https://github.com/GRaCe-GRC/.github/blob/main/CONTRIBUTING.md).
Commit messages use Conventional Commits with the ticket ID, for example
`feat: GRC-14: add <change>`.

## License
Licensed under the [Apache License 2.0](LICENSE).
