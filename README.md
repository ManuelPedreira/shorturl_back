# Short URL Service

#### Work in progress

A simple URL shortener built with Spring Boot. It creates short codes for long URLs, redirects users to the original destination, generates bot-friendly previews using page metadata, and stores access telemetry.

### Features
- Shorten long URLs into 7‑char short codes
- 307 redirect for users
- Minimal HTML for bots (title, description, image)
- Async metadata extraction from target pages
- Safe URL validation to prevent SSRF
- Telemetry collection (WIP)
- User link management and telemetry (WIP)

## Quick Start

```bash
mvn spring-boot:run
```
Runs at `http://localhost:8080`.

## API

### Create short URL
```http
POST /api
Content-Type: application/json

{
  "url": "https://example.com/"
}
```
Response
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
Behavior
- Users: HTTP 307 Temporary Redirect to the original URL
- Bots: minimal HTML with metadata for link previews

Metadata extraction and telemetry are processed asynchronously.

## Security
- Public: `POST /api`, `GET /{shortCode:[a-zA-Z0-9]{7}}`
- All other paths are protected
- Ownership model (planned): users can manage their own links and telemetry

## Database
- `urls`: short code, original URL, metadata, timestamps, optional owner
- `telemetry`: visit logs (event time, IP, user agent, country)
- `users` (WIP): accounts for link ownership/management

## Roadmap
- Authentication, dashboards, and ownership enforcement
- Telemetry reports and filters
- Custom domains and vanity codes
- Rate limiting and abuse prevention

## Tech
Spring Boot 3, Spring Data JPA, Spring Security, Thymeleaf, PostgreSQL, Jsoup.
