# **ICoder Backend**

ICoder is a competitive programming platform designed to provide an integrated experience for problem solving, contests, groups, and collaborative learning—similar to platforms like Codeforces, AtCoder, and LeetCode.
This repository contains **only the backend services** for the ICoder platform.
The frontend is maintained in a separate repository.

---

## **Table of Contents**

* [Overview](#overview)
* [Features](#features)
* [Architecture](#architecture)
* [Project Structure](#project-structure)
* [Tech Stack](#tech-stack)
* [Setup & Installation](#setup--installation)
* [API Documentation](#api-documentation)
* [Contributing](#contributing)

---

## **Overview**

The ICoder backend provides REST APIs to support user authentication, problem fetching from external judges, contest handling, group management, submission evaluation, notifications, and more. It integrates multiple external services to synchronize programming problems and submissions.

---

## **Features**

* Provide secure authentication with email verification
* Integrate with Codeforces, AtCoder, UVa, and other online judges
* Manage problems with search, filter, and metadata syncing
* Support LeetCode-style coding editor
* Create, schedule, and participate in contests with live leaderboard
* Manage groups with leader, manager, and member roles
* Track submission history and remote judge evaluation
* Send notifications for invitations, verifications, and system events
* Collaborative meeting system *(planned/future feature)*

---

## **Architecture**
_This section is under development and will be updated once modules are finalized._


---

## **Project Structure**
_This section will be added once the directory structure and modules stabilize._

---

## **Tech Stack**

- ![Java](https://img.shields.io/badge/Language-Java%2017-orange)
- ![SpringBoot](https://img.shields.io/badge/Framework-Spring%20Boot%203.3.4-brightgreen)
- ![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue)
- ![JPA](https://img.shields.io/badge/ORM-Spring%20Data%20JPA%20%2F%20Hibernate-purple)
- ![Maven](https://img.shields.io/badge/Build-Maven-yellow)
- ![Security](https://img.shields.io/badge/Security-Spring%20Security%20%7C%20JWT-red)
- ![MapStruct](https://img.shields.io/badge/Mapping-MapStruct%201.5.5.Final-lightgrey)
- ![OpenAPI](https://img.shields.io/badge/Documentation-SpringDoc%20OpenAPI%202.3.0-blueviolet)
- ![Email](https://img.shields.io/badge/Email-Spring%20Mail%20(Gmail%20SMTP)-orange)
- ![Scraping](https://img.shields.io/badge/Web%20Scraping-Coming%20Soon-lightgrey)
- ![ExternalAPIs](https://img.shields.io/badge/External%20APIs-Coming%20Soon-lightgrey)
- ![Docker](https://img.shields.io/badge/Deployment-Docker%20%7C%20Containers-blue)

---

# **Setup & Installation**
```bash
# Clone the repository and Navigate to the project
git clone https://github.com/roaa46/ICoder-backend.git
cd ICoder-backend
```

### **Environment Variables**
Copy `env.properties.example` → rename it to `env.properties` → fill in your configuration values.

### Running with Docker

Notes:

- Uploaded files (profile pictures, etc.) will be stored in the local ./uploads folder.

- Make sure .env is configured before running Docker.

- The application will be available at `http://localhost:${PORT}`.

```bash
# Build image
docker-compose build
# Start container
docker-compose up -d
# Stop container
docker-compose down
# View logs
docker-compose logs -f
```

### **Running Local / Maven**

```bash
# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

---

# **API Documentation**

API docs (Swagger/OpenAPI) will be available at:

Once the application is running, access the Swagger UI documentation at: `http://localhost:${PORT}/swagger-ui.html`

or navigate to `docs/api`


---

# **Contributing**

Contributions are welcome!
Please submit a pull request with proper commit messages and follow the project’s guidelines.

