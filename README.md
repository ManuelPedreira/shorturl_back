# ShortURL Backend

Spring service that creates 7-character short codes, issues 307 redirects, and streams scraped metadata over STOMP/SockJS, can surface bot-friendly previews while also logging telemetry for every redirected short URL.

## Repositories
- Frontend (`shorturl_front`): https://github.com/ManuelPedreira/shorturl_front
- Backend (`shorturl_back`): https://github.com/ManuelPedreira/shorturl_back

## System Overview
- Frontend and backend run behind a shared Nginx reverse proxy that enforces rate limits and routes traffic to the proper upstream.
- UI is a Next.js App Router project (`shorturl_front`).
- Backend (`shorturl_back`) is a Spring service that exposes REST + STOMP endpoints.
- Nginx (listening on `:80`) forwards:
  - `/` and `/success` to the frontend.
  - `/api`, `/ws`, and short-code slugs like `/abc1234` to the backend with WebSocket upgrade headers.

## Features
- Shorten long URLs into 7-char short codes with collision retries.
- 307 redirects for users; bots get minimal HTML (`botPage.html`) with title/description/image.
- Async metadata extraction with Jsoup, sanitization, and STOMP push so the frontend updates instantly.
- Safe URL validator blocks SSRF attempts, loopback/private IPs, unexpected ports, and host callbacks.
- Telemetry collection: async visit logging, link management and telemetry views planned.
- WS pipeline: SockJS endpoint `/ws`, JWT-protected handshake (`WSAccess` cookie), per-topic authorization, and an in-memory buffer.
- Problem Details errors plus custom access/entry handlers aligned with Spring Security; future user/link management hooks remain WIP.

## Getting Started

```bash
mvn spring-boot:run
# http://localhost:8080
```

## REST API

### Create short URL
```http
POST /api
Content-Type: application/json

{
  "url": "https://example.com/"
}
```
Response (201 + `Location`):
```json
{
  "shortCode": "mjX3ndN",
  "originalUrl": "https://example.com/",
  "shortUrl": "http://localhost:8080/mjX3ndN"
}
```

### Resolve short code
```http
GET /mjX3ndN
```
- Humans: HTTP 307 redirect + async telemetry.
- Bots (matches `custom.bot-agent`): `botPage.html` with stored metadata.

## WebSocket / STOMP
- Endpoint: `GET /ws` (SockJS, upgrades when possible).
- Topics: `/topic/url.{shortCode}` only. Messages carry `status = done|error` and expire after `custom.websocket.expirationTime.seconds`.
- Security: `CookieWSHandshakeInterceptor` checks `WSAccess` JWT (`JWT_SECRET`), `SubscriptionWSValidationInterceptor` enforces topic/code match.
- `SubscriptionEventListener` replays buffered payloads.

## Security
- Spring Security permits `POST /api`, `GET /{shortCode}`, `GET /ws/**`; everything else stays authenticated-ready.
- Safe URL validation, Problem Details responses, JWT-gated WebSocket access.

## Database
- `urls`: short code, original URL, metadata, timestamps, optional owner
- `telemetry`: visit logs (event time, IP, user agent, country)
- `users` (WIP): accounts for link ownership/management

## Environment Variables

| Key | Default | Description |
| --- | --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASS` | – | JDBC connection + credentials. |
| `DB_C_DLL`, `DB_C_LOG` | `update`, `false` | Hibernate DDL + SQL logging. |
| `DB_POOL_MAX`, `DB_POOL_MIN` | `5`, `1` | Hikari pool sizing. |
| `CORS_ORIGIN` | `http://localhost:3000` | Allowed origin for `/api/**`. |
| `PUBLIC_SERVER_HOST` | `http://localhost:8080` | Used to build `shortUrl` and prevent SSRF to self. |
| `JWT_SECRET` | – | Secret for `WSAccess` JWT. |
| `PORT` | `8080` | HTTP port. |

## Roadmap
- Authentication, dashboards, and user panel
- Telemetry reports and filters
- Custom url codes

## Tech Stack
- **Framework**: Spring Boot 3.5 (Web, Security, Validation, WebSocket, Thymeleaf).
- **Data**: Spring Data JPA + PostgreSQL.
- **Realtime**: SockJS/STOMP, `SimpMessagingTemplate`, async executors.
- **Security**: Spring Security, `io.jsonwebtoken`, Problem Details, CORS.
- **Parsing**: Jsoup for metadata scraping and cleanup.
