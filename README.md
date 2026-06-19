# **ICoder Backend**

[![Java](https://img.shields.io/badge/Language-Java%2017-orange)](https://www.oracle.com/java/)
[![SpringBoot](https://img.shields.io/badge/Framework-Spring%20Boot%203.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Cache-Redis-DC382D)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/Messaging-RabbitMQ-FF6600)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Deployment-Docker-blue)](https://www.docker.com/)
[![CI/CD](https://github.com/roaa46/ICoder-Backend/actions/workflows/main.yml/badge.svg)](https://github.com/roaa46/ICoder-Backend/actions)
[![Container Registry](https://img.shields.io/badge/GHCR-icoder--backend-blue?logo=docker)](https://github.com/roaa46/ICoder-Backend/pkgs/container/icoder-backend)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/roaa46/ICoder-Backend)

**ICoder** is a full-featured backend for a competitive programming platform — bringing problem solving, contests, group
collaboration, meeting system
, and AI-assisted progress tracking together in one system, in the spirit of Codeforces, AtCoder, and CSES.

This repository contains **only the backend**. The frontend is maintained in a separate repository.

Full architecture and module documentation: **[DeepWiki](https://deepwiki.com/roaa46/ICoder-Backend)**

---

## Table of Contents

* [Overview](#overview)
* [Key Features](#key-features)
* [Architecture](#architecture)
* [Module Structure](#module-structure)
* [Tech Stack](#tech-stack)
* [Getting Started](#getting-started)
* [Running with Docker](#running-with-docker)
* [Running Locally with Maven](#running-locally-with-maven)
* [API Documentation](#api-documentation)
* [Testing](#testing)
* [Contributing](#contributing)
* [License](#license)

---

## Overview

ICoder's backend exposes a REST API that integrates several external online judges (Codeforces, AtCoder, CSES)
and a code-execution engine (Judge0) to provide a unified competitive programming experience.
It manages the complete lifecycle of a competitive programmer: registration and authentication, problem discovery,
contest participation with live leaderboards,
group-based collaboration, submission evaluation, meeting sessions, and AI-driven performance analysis.

---

## Key Features

- **Secure Authentication** — JWT-based stateless auth with email verification, refresh tokens, and Redis-backed token
  revocation
- **Multi-Judge Integration** — Synchronizes problems and submissions from Codeforces, AtCoder, and CSES
- **Problem Management** — Search, filter, and cache problem metadata and statements
- **Online Code Editor** — LeetCode-style code execution powered by Judge0, with reusable code templates
- **Contests** — Create, schedule, and join contests (including password-protected ones), with live leaderboards and
  real-time streaming via SSE
- **Group Collaboration** — Groups with owner/manager/member roles, privacy controls, and invitations
- **Meetings** — Collaborative group meeting management
- **Submission Tracking** — Asynchronous submission pipeline with RabbitMQ queuing and remote judge polling .
- **AI Performance Summaries** — Groq API-powered analysis of a user's problem-solving progress
- **Activity Tracking** — Submission heatmaps and streaks
- **Notifications** — Real-time (WebSocket) and persistent notifications for invitations, verifications, and system
  events

---

## Architecture

ICoder follows a **modular, layered architecture**. Each functional domain is isolated into its own package but shares
common infrastructure (security, caching, exception handling) defined in the `core` module.

Every module follows the same three-tier pattern:

| Layer          | Responsibility                                                             |
|----------------|----------------------------------------------------------------------------|
| **Controller** | Exposes REST endpoints, validates requests, returns standardized responses |
| **Service**    | Implements business logic; coordinates repositories and external services  |
| **Repository** | Persists data via Spring Data JPA, backed by PostgreSQL                    |

Cross-cutting concerns:

- **Stateless authentication** via JWT, with revocation state cached in Redis (`jwt:token:{token}`)
- **Caching** of problem metadata (7 days) and problem statements (30 days) in Redis
- **Async processing** of submissions and judge polling through RabbitMQ
- **Shared base entity** for consistent ID handling and Hibernate-safe equality checks

---

## Module Structure

The codebase lives under the root package `com.icoder`, organized by domain:

| Module           | Package                   | Responsibility                                          |
|------------------|---------------------------|---------------------------------------------------------|
| **User**         | `com.icoder.user`         | Authentication, JWT management, profile services        |
| **Problem**      | `com.icoder.problem`      | External judge scraping engine, problem metadata        |
| **Submission**   | `com.icoder.submission`   | Async submission pipeline, OJ providers, result polling |
| **Contest**      | `com.icoder.contest`      | Contest lifecycle, problem sets, real-time leaderboards |
| **Group**        | `com.icoder.group`        | Collaborative groups, member roles, privacy             |
| **Invitation**   | `com.icoder.invitation`   | Group and system invitation lifecycle                   |
| **Coding**       | `com.icoder.coding`       | Judge0-based code execution, code templates             |
| **Meeting**      | `com.icoder.meeting`      | Group video session and meeting management              |
| **Summary**      | `com.icoder.summary`      | AI-powered performance analysis and statistics          |
| **Activity**     | `com.icoder.activity`     | Activity logs, submission heatmaps and streaks          |
| **Notification** | `com.icoder.notification` | WebSocket and persistent notifications                  |
| **Core**         | `com.icoder.core`         | Shared security, caching, exceptions, base entities     |

> For a deeper breakdown of each module's API, data model, and internal flow, see
> the [DeepWiki documentation](https://deepwiki.com/roaa46/ICoder-Backend).

---

## Tech Stack

| Category           | Technology                                       |
|--------------------|--------------------------------------------------|
| Language           | Java 17                                          |
| Framework          | Spring Boot 3.3.4                                |
| Database           | PostgreSQL (via Spring Data JPA / Hibernate)     |
| Caching & Sessions | Redis                                            |
| Messaging          | RabbitMQ (async submission processing & polling) |
| Security           | Spring Security, JWT                             |
| Object Mapping     | MapStruct 1.5.5.Final                            |
| API Documentation  | SpringDoc OpenAPI 2.3.0 (Swagger)                |
| Email              | Spring Mail (Gmail SMTP)                         |
| Web Scraping       | Playwright, Jsoup                                |
| Code Execution     | Judge0                                           |
| Media Storage      | Cloudinary                                       |
| AI Integration     | Groq API                                         |
| Build Tool         | Maven                                            |
| Containerization   | Docker / Docker Compose                          |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- Docker & Docker Compose (recommended for local setup)
- PostgreSQL, Redis, and RabbitMQ (provisioned automatically if using Docker Compose)

### Clone the Repository

```bash
git clone https://github.com/roaa46/ICoder-Backend.git
cd ICoder-Backend
```

### Configure Environment Variables

Copy the example environment file and fill in your own values (database credentials, JWT secret, Cloudinary keys, Groq
API key, SMTP credentials, etc.):

```bash
cp env.properties.example env.properties
```

Edit `env.properties` with your configuration before running the application.
 
---

## Running with Docker

This is the recommended way to run the full stack (app + PostgreSQL + Redis + RabbitMQ) with minimal setup.

### Option A: Build from Source

```bash
# Build the image
docker compose build
 
# Start the containers
docker compose --env-file env.properties up -d
 
# View logs
docker compose logs -f
 
# Stop the containers
docker compose down
```

### Option B: Use the Pre-built Image

Every push is automatically built and published to GitHub Container Registry via CI/CD. You can pull the latest image
directly instead of building it yourself:

```bash
docker pull ghcr.io/roaa46/icoder-backend:latest
```

Then update the `image:` field for the app service in `docker-compose.yml` to `ghcr.io/roaa46/icoder-backend:latest` and
run:

```bash
docker compose --env-file env.properties up -d
```

> Browse all published tags on
> the [GitHub Container Registry page](https://github.com/roaa46/ICoder-Backend/pkgs/container/icoder-backend).

**Notes:**

- Make sure `env.properties` is configured **before** starting the containers.
- Once running, the application is available at `http://localhost:${PORT}`.

## Running Locally with Maven

If you prefer to run the application directly (with PostgreSQL, Redis, and RabbitMQ running separately):

```bash
# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run
 
# Linux / macOS
./mvnw clean install
./mvnw spring-boot:run
```

 
---

## API Documentation

Once the application is running, interactive API documentation (Swagger UI) is available at:

```
http://localhost:${PORT}/swagger-ui.html
```

The raw OpenAPI spec can also be found at `docs/api-docs.json`.

---

## Testing

Run the test suite with:

```bash
./mvnw test
```

 
---

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes with clear, descriptive messages
4. Push to your branch and open a Pull Request
   Please follow the project's existing code style and structure when submitting changes.

## License

This project is part of a graduation project and is currently unlicensed for public distribution. Contact the repository
owner for usage permissions.