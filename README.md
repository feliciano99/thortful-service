# Thortful — Greeting Cards Service

A small, runnable full‑stack service centred on a **listing page that comfortably handles 1,000+
items**. A Spring Boot 4 / Java 25 backend exposes a searchable, paginated REST API; an Angular 22 +
Angular Material frontend consumes it through an Nginx reverse proxy. Everything builds and runs with
a single command.

---

## Quick start

> Requires only a container engine. **Docker Desktop or Podman Desktop** both work — no local JDK,
> Maven, or Node needed (everything compiles inside the build images).

```bash
docker compose up --build
```

Then open **http://localhost:8080** and sign in with the default credentials below.

| What | URL |
|------|-----|
| Web UI | http://localhost:8080 |
| Swagger UI | http://localhost:8080/cards/v1/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/cards/v1/v3/api-docs |
| Health | http://localhost:8080/cards/v1/actuator/health |

The backend seeds **1,200 realistic cards** on first start, so the list is populated immediately.

### Default credentials

The cards API is protected with HTTP Basic auth. Credentials come from environment variables with
safe fallbacks:

| Variable | Compose default | IDE‑only fallback |
|----------|-----------------|-------------------|
| `APP_AUTH_USER` | `admin` | `admin` |
| `APP_AUTH_PASSWORD` | `changeme` | `admin` |

So out of the box (via Compose) sign in with **`admin` / `changeme`**.

### Configuration & secrets

Secrets are externalised. Copy the template and edit if you want to override the defaults:

```bash
cp .env.example .env
```

`.env` is git‑ignored. Compose reads it for variable substitution and injects the values into the
containers. Because every variable has a `${VAR:-default}` fallback, **`docker compose up --build`
works even with no `.env` file** — the `.env` only overrides for real secrets.

---

## Architecture

```
Browser ──▶ Nginx (frontend container, :80 → host :8080)
              ├── serves the Angular SPA (static files)
              └── reverse‑proxies /cards/* ──▶ Backend (Spring Boot, :8080)
                                                  └──▶ PostgreSQL
```

- **No CORS in the Java layer** — the browser only ever talks to one origin (Nginx). Same‑origin
  requests to `/cards/*` are proxied to the backend, so cross‑origin config is unnecessary.
- **Service‑namespaced context path** — the backend runs under `server.servlet.context-path =
  /${spring.application.name}/v1` = **`/cards/v1`**, so the whole service (API, actuator, Swagger) is
  reachable under `/cards/*` and the proxy rule stays a single `location /cards/`.
- **Health‑gated startup** — Compose orders boot with health checks: Postgres (`pg_isready`) →
  backend (`/cards/v1/actuator/health`) → frontend, each `depends_on … condition: service_healthy`,
  eliminating startup races. Forwarded headers (`server.forward-headers-strategy: framework`) make
  the backend honour the proxy's host/scheme.

### Project layout

```
thortful-service/
├── docker-compose.yml        # postgres → backend → frontend, health‑gated
├── .env.example              # secret template (.env is git‑ignored)
├── backend/                  # Spring Boot 4 / Java 25 (Maven)
│   ├── Dockerfile            # multi‑stage JDK build → JRE runtime
│   └── src/main/java/com/thortful/cards/
│       ├── domain/           # GreetingCard, Category, StockStatus, CardNotFoundException
│       ├── application/       # CardService (use cases), DTO records, MapStruct mapper
│       └── infrastructure/    # web (controllers, advice, security filter), persistence, config, seed
└── frontend/                 # Angular 22 (standalone) + Angular Material
    ├── Dockerfile            # multi‑stage node build → nginx serve
    ├── nginx.conf            # SPA + /cards/* reverse proxy
    └── e2e/                  # Playwright CRUD spec
```

### Backend layering (DDD + SOLID)

- **domain** — the model and its rules (`GreetingCard` entity, `Category` / `StockStatus` enums,
  `CardNotFoundException`). Free of web concerns.
- **application** — use cases (`CardService`: search / create / delete), immutable DTO `record`s
  (`GreetingCardResponse`, `CreateCardRequest`, `CategoryResponse`), and a MapStruct mapper. Each
  class has one responsibility; error translation, HTTP, and persistence are kept out.
- **infrastructure** — adapters: REST controllers, the global exception advice, Spring Security, the
  Spring Data repository + JPA `Specification`s, OpenAPI config, and the DataFaker seeder.

The Spring Data repository lives in `infrastructure` (it's bound to JPA — `Pageable`,
`Specification`), rather than introducing a hand‑rolled domain port + adapter that would add no value
at this scale. This is the "no useless abstraction" trade‑off the brief asks for.

---

## API

All under the `/cards/v1` context path. `/cards/**` requires Basic auth; categories, actuator and
Swagger are open.

| Method | Path | Notes |
|--------|------|-------|
| `GET` | `/cards/v1/cards` | Paginated. Params: `page`, `size` (≤100), `sort` (e.g. `title,asc`), `search` (title contains, case‑insensitive), `category` (enum filter) |
| `POST` | `/cards/v1/cards` | Create. Bean‑validated body; returns `201` + `Location` |
| `DELETE` | `/cards/v1/cards/{id}` | Delete; `204`, or `404` if absent |
| `GET` | `/cards/v1/categories` | Category values + display labels (drives the UI filter) |

The list returns a stable `PagedModel` shape: `{ content: [...], page: { size, number, totalElements,
totalPages } }`. Errors return a uniform `ApiError` record (timestamp, status, error, message, path,
`requestId`, optional `fieldErrors`).

---

## High‑performance listing

- **Server‑side everything** — search, filter, sort and pagination run in the database via Spring
  Data `Pageable` + `JpaSpecificationExecutor`; the browser never loads more than one page.
- **Indexes** — the Flyway migration adds a `category` index and a functional `lower(title)` index so
  the case‑insensitive title search and category filter stay index‑friendly.
- **DTO projection** — the controller maps the entity `Page` to a DTO `Page` with the native
  `.map()` and wraps it in `PagedModel` for a stable contract.
- **Debounced, reactive frontend** — the search box uses RxJS `debounceTime(300)` +
  `distinctUntilChanged()`; loading state is shown with a Material progress bar; state is held in
  Angular signals.

---

## Java 25 in use

- **Virtual threads** (`spring.threads.virtual.enabled=true`) and **graceful shutdown**
  (`server.shutdown=graceful`).
- **Scoped Values (JEP 506)** — a per‑request correlation id is bound via `ScopedValue` in a servlet
  filter and surfaced as the `X-Request-Id` header and in every `ApiError` (instead of `ThreadLocal`).
- **Stream Gatherers (JEP 485)** — the seeder batches its 1,200 inserts with
  `Gatherers.windowFixed(...)`.
- **Pattern matching for `switch`** — the `@RestControllerAdvice` maps exception types to HTTP status
  in a single switch.
- **Records** for all DTOs; **unnamed variables** (`_`) for unused lambda parameters.

---

## Security

- **Spring Security** `SecurityFilterChain`: HTTP Basic on `/cards/**`, stateless sessions, CSRF
  disabled (token‑less API). Credentials are the env‑backed in‑memory user.
- **Frontend interceptor** — a functional `HttpInterceptorFn` attaches the Base64 Basic token from an
  `AuthService` that stores it in **transient `sessionStorage`** (never compiled into the bundle). A
  `401` triggers logout + redirect to the login form.
- **Playwright** authenticates headlessly via native **`httpCredentials`** mapped from
  `APP_AUTH_USER` / `APP_AUTH_PASSWORD`, bypassing the login form at the network layer.

---

## Testing

**Backend** (JUnit 5 + Mockito unit tests; Testcontainers integration tests — needs a running
container engine for Testcontainers):

```bash
cd backend
./mvnw verify
```

- Unit: `CardService`, `GlobalExceptionHandler`.
- Integration: repository search/paging (`@DataJpaTest` + Testcontainers Postgres) and the full API
  (`@SpringBootTest`, authenticated, incl. a 401 check).

**Frontend E2E** (Playwright) — against a running stack:

```bash
cd frontend
npm ci
npx playwright install --with-deps chromium
E2E_BASE_URL=http://localhost:8080 npm run e2e
```

Covers server‑side pagination, the full create → search → delete workflow, and add‑form validation.

---

## Trade‑offs

- **HTTP Basic auth** was chosen for simplicity (per the brief). It has no token expiry and sends
  credentials on every request — acceptable here behind the proxy, but production needs TLS + a
  proper token scheme (see checklist). A single in‑memory user means no user management.
- **`Category` is a code enum**, giving type safety and a clean filter source. A catalogue whose
  taxonomy is edited at runtime would use a reference table instead.
- **JPA annotations live on the domain entity** (pragmatic DDD) rather than a separate persistence
  model + mapper — that abstraction isn't worth its weight at this scale.
- **Title search uses `LIKE` on `lower(title)`** — perfect for ~1,200 rows; a much larger catalogue
  would want Postgres full‑text (`tsvector`) or a search engine.
- **DTOs are duplicated** front/back rather than generated from the OpenAPI schema — fine for this
  size; codegen would remove the drift risk as it grows.
- The Basic token is kept in `sessionStorage` for convenience; a hardened build would keep it purely
  in memory or move to short‑lived bearer tokens.

### Future production‑readiness checklist

- [ ] Replace HTTP Basic with OAuth2/OIDC + short‑lived JWTs (or an external IdP); per‑user accounts.
- [ ] TLS termination + HSTS at the proxy; security headers (CSP, X‑Frame‑Options) and SRI for the SPA.
- [ ] Secrets from a vault/secret manager (not `.env`); credential rotation.
- [ ] Observability: Prometheus metrics, OpenTelemetry tracing (the `requestId` is already a seam),
      structured JSON logs, dashboards and alerts.
- [ ] Rate limiting, request‑size limits, and an audit log for create/delete.
- [ ] Keyset/cursor pagination for very large datasets; caching + ETags for `categories`.
- [ ] CI/CD running `mvn verify` + Playwright on every PR; image scanning and an SBOM.
- [ ] Postgres connection‑pool tuning, automated backups + PITR, and read replicas if needed.
- [ ] Kubernetes liveness/readiness probes, resource limits, and horizontal scaling.
- [ ] Frontend error monitoring, accessibility audit, and i18n.

---

## AI assistance

This solution was built with AI pair‑programming (Claude), incrementally and with a manual commit
after each step for review. The prompts and assistant transcripts used during development are
included alongside this repository as part of the submission.

---

## If I had more time

- A `GET /cards/{id}` endpoint so the `Location` header resolves to a retrievable resource.
- Edit/update flow, optimistic concurrency, and bulk operations.
- A shared OpenAPI‑generated TypeScript client to remove DTO duplication.
- Skeleton loaders instead of a progress bar, and category counts in the filter.
