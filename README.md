# EV Charging Platform

An enterprise-grade, high-performance **EV Charging Management Platform** built as a Spring Boot 3.4 Modular Monolith in Java 21 with a modern TypeScript/Vite frontend.

---

## Architecture & Technology Stack

- **Backend**: Java 21, Spring Boot 3.4.1, Spring Data JPA, Spring Security (Stateless JWT), Spring WebFlux
- **Database**: PostgreSQL 16 + PostGIS extension, Flyway DB Migrations
- **Cache**: Redis 7
- **API Documentation**: SpringDoc OpenAPI 2.8.3, Swagger UI
- **Frontend**: React, TypeScript, Vite, Tailwind CSS

---

## Prerequisites

Before running the platform, ensure you have installed:
- **Java 21 JDK** (e.g. Eclipse Temurin 21)
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Node.js 20+** (if running the frontend locally)

---

## Running the System

### 1. Start Infrastructure (PostgreSQL & Redis)

Start the required database and cache containers using Docker Compose:

```bash
docker-compose up -d postgres
```

Verify that PostgreSQL (port `5432`) and Redis (port `6379`) are running:

```bash
docker-compose ps
```

### 2. Run the Backend Application

Navigate to the `backend/` directory and compile the project:

```bash
cd backend
mvn clean install -DskipTests
```

Run the composition root application (`evcharging-app`):

```bash
mvn spring-boot:run -pl evcharging-app
```

The application will initialize Flyway database migrations and start Tomcat on **port 8080**.

---

## Default Superuser / Admin Credentials

The system seeds a default **Platform SuperAdmin** account on startup via Flyway database migration:

| Setting | Value |
| :--- | :--- |
| **Role** | `ADMIN` (Platform SuperAdmin) |
| **Email** | `superadmin@evcharging.test` |
| **Password** | `SuperAdmin@Pass1!` |

> [!IMPORTANT]  
> You can use this SuperAdmin account to log in, obtain an admin JWT token, register vendors, configure station markups, register secondary admin accounts (`POST /api/v1/identity/auth/register-admin`), and view platform analytics dashboard.

---

## Accessing Swagger UI & API Specs

Once the backend is running, open your web browser:

- **Swagger UI Interactive Dashboard**:  
  👉 **`http://localhost:8080/swagger-ui.html`**

- **OpenAPI 3.0 Spec (JSON)**:  
  👉 **`http://localhost:8080/api-docs`**

---

## Using Swagger UI for Testing

Swagger UI provides an interactive dashboard to test all public and protected APIs.

### Step 1: Log In as SuperAdmin or Register a Customer

#### Option A: Log In as SuperAdmin (Recommended for Admin Operations)

1. Open `http://localhost:8080/swagger-ui.html`.
2. Expand **Identity & Access Management** -> **`POST /api/v1/identity/auth/login`**.
3. Click **Try it out** and execute:

```json
{
  "email": "superadmin@evcharging.test",
  "password": "SuperAdmin@Pass1!"
}
```

#### Option B: Register a New Customer Account (For Driver Operations)

1. Expand **`POST /api/v1/identity/auth/register-customer`**.
2. Click **Try it out** and execute:

```json
{
  "name": "Alex Driver",
  "email": "alex.driver@example.com",
  "password": "Password123!",
  "phone": "+12025550199"
}
```

---

### Step 2: Authenticate & Obtain JWT Token

1. Execute **`POST /api/v1/identity/auth/login`** using either SuperAdmin credentials or your customer credentials.
2. Copy the `accessToken` string from the JSON response:

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "..."
  }
}
```

---

### Step 3: Authorize Swagger UI Requests

1. Click the green **Authorize 🔓** button at the top right of the Swagger UI page.
2. In the **bearerAuth (http, Bearer)** input box, paste:
   ```text
   Bearer eyJhbGciOiJIUzI1NiJ9...
   ```
3. Click **Authorize**, then click **Close**. All subsequent requests sent from Swagger UI will automatically include the `Authorization` header.

---

### Step 4: Test Core Platform APIs

Now you can execute protected operations:

#### 🚗 Vehicle Management
- **`POST /api/v1/vehicles`**: Register a new EV vehicle under your driver account.
- **`GET /api/v1/vehicles`**: List all active vehicles registered to your account.

#### 🔌 Station Management
- **`GET /api/v1/stations`**: Search available charging stations and view connector details.

#### ⚡ Charging Sessions
- **`POST /api/v1/sessions`**: Start a charging session (`stationId`, `connectorId`, `customerId`, `vehicleId`).
- **`POST /api/v1/sessions/{id}/meter-readings`**: Record periodic energy telemetry (`energyDeliveredKwh`, `powerKw`).
- **`POST /api/v1/sessions/{id}/stop`**: Complete a charging session and calculate total energy cost.

#### 📄 Billing & Invoices
- **`GET /api/v1/billing/invoices/session/{sessionId}`**: Retrieve line-item breakdown and invoice details for a session.

---

## Running the Frontend

To launch the web interface:

```bash
cd frontend
npm install
npm run dev
```

Open **`http://localhost:5173`** in your browser.
