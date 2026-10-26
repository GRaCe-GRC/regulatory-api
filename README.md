# regulatory-api

Inner API for regulatory files and requirements (Regulatory database).

> **Status:** CRUD for regulatory authorities, frameworks, files and requirements in the `regulatory` database.

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
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Authorities API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/authorities` | `name`, `jurisdiction`, `createdBy` | `201`, empty body, `Location` header with the new authority's URL |
| `GET /api/v1/authorities` | | `200` with the list of authorities |
| `GET /api/v1/authorities/{id}` | | `200` with the authority |
| `PUT /api/v1/authorities/{id}` | `name`, `jurisdiction`, `enabled` | `200` with the authority (`createdAt` and `createdBy` never change) |
| `DELETE /api/v1/authorities/{id}` | | `204` |

New authorities are `enabled`. Errors use RFC 9457 problem details: `400` for invalid input
(with an `errors` field per property), `404` for an unknown id, `409` for a duplicate name or when
deleting an authority that still has frameworks.

```bash
curl -s -H 'Content-Type: application/json' \
  -d '{"name":"European Banking Authority","jurisdiction":"EU","createdBy":"me"}' \
  http://localhost:8080/api/v1/authorities
```

## Frameworks API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/frameworks` | `authorityId`, `name`, `shortName`, `citation`, `createdBy` | `201`, empty body, `Location` header with the new framework's URL |
| `GET /api/v1/frameworks?authorityId=` | | `200` with the list of frameworks, all of them or only the authority's |
| `GET /api/v1/frameworks/{id}` | | `200` with the framework |
| `PUT /api/v1/frameworks/{id}` | `name`, `shortName`, `citation`, `enabled` | `200` with the framework (`authorityId`, `createdAt` and `createdBy` never change) |
| `DELETE /api/v1/frameworks/{id}` | | `204` |

`shortName` and `citation` are optional. New frameworks are `enabled`. Errors: `404` for an unknown
id or `authorityId`, `409` for a name that the authority already uses or when deleting a framework
that still has files.

## Files API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/files` | `frameworkId`, `title`, `sourceUrl`, `versionLabel`, `publicationDate`, `createdBy` | `201`, empty body, `Location` header with the new file's URL |
| `GET /api/v1/files?frameworkId=` | | `200` with the list of files, all of them or only the framework's |
| `GET /api/v1/files/{id}` | | `200` with the file |
| `PUT /api/v1/files/{id}` | `title`, `sourceUrl`, `versionLabel`, `publicationDate` | `200` with the file (`frameworkId`, `createdAt` and `createdBy` never change) |
| `DELETE /api/v1/files/{id}` | | `204` |

A file is one published version of a framework. `sourceUrl`, `versionLabel` and `publicationDate`
(`YYYY-MM-DD`) are optional. Errors: `404` for an unknown id or `frameworkId`, `409` when deleting
a file that still has requirements.

## Requirements API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/requirements` | `fileId`, `parentRequirementId`, `provisionRef`, `heading`, `text`, `createdBy` | `201`, empty body, `Location` header with the new requirement's URL |
| `GET /api/v1/requirements?fileId=&parentId=` | | `200` with the list of requirements; each filter is optional |
| `GET /api/v1/requirements/{id}` | | `200` with the requirement |
| `PUT /api/v1/requirements/{id}` | `provisionRef`, `heading`, `text` | `200` with the requirement (`fileId`, `parentRequirementId`, `createdAt` and `createdBy` never change) |
| `DELETE /api/v1/requirements/{id}` | | `204` |

A requirement is one provision of a file, for example `164.308(a)(4)(i)`. Requirements form a tree
inside the file: `parentRequirementId` is optional and must be a requirement of the same file.
`heading` and `text` are optional. Errors: `400` when the parent is in another file, `404` for an
unknown id, `fileId` or `parentRequirementId`, `409` for a `provisionRef` that the file already
uses or when deleting a requirement that still has child requirements.

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
| `frameworks` | `V2__create_frameworks.sql` |
| `files` | `V3__create_files.sql` |
| `requirements` | `V4__create_requirements.sql` |

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
