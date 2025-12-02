# **PrevenFire IoT – Temperature Monitoring & Alerting Platform**

PrevenFire IoT is a complete proof-of-concept platform for real-time temperature monitoring and alerting.
It combines **Java 17**, **Spring Boot microservices**, **API Gateway**, **PostgreSQL**, **Docker**, and a **React Native (Expo)** mobile app—fully integrated with an **ESP32** embedded device.

This project was built with a strong focus on **clean architecture**, **separation of concerns**, **scalability**, **solid backend design**, and **mobile best practices** (debounce, loading states, UX feedback).
Although its visual identity focuses on fire-prone environments (kitchens, offices, warehouses), the architecture is flexible enough to support scenarios such as cold rooms, server racks, greenhouses, heated pools, and more.

---

## 🚀 **Features**

### 🔧 **Centralized IoT Device Configuration**

* Configure temperature limits, reading intervals, and high-tolerance mode directly from the mobile app.
* Backend automatically computes the `effectiveTemperatureLimit` when tolerance mode is active.

### 📡 **Real-Time Sensor Logging**

* ESP32 sends periodic temperature readings and applied limit.
* Full per-device history + filtered view for critical (over-limit) events.

### 📱 **Mobile App (React Native / Expo)**

* Config screens for device ID, limit, interval, and tolerance.
* Real-time reading list with visual critical indicators.
* Emergency shortcut (193).
* UX optimized with debounce and status messages.

### 🧩 **Microservices + API Gateway**

* **Control Service** → device configurations
* **Logging Service** → reading history
* **API Gateway** → single unified entry point for mobile + embedded device

### 📦 **Containerized Infrastructure**

* Two separate PostgreSQL databases.
* Full orchestration via Docker Compose with isolated networking.

---

## 🏗️ **Architecture Overview**

### **Embedded Device (ESP32)**

* Reads temperature from a sensor.
* Fetches config from Control Service.
* Sends periodic readings to Logging Service.

---

### **Backend Services (Java / Spring Boot)**

#### **API Gateway (Port 8080)**

Routes:

* `/api/config/**` → Control Service
* `/api/readings/**` → Logging Service

#### **Control Service (Port 8082)**

* CRUD operations for device configs.
* Upsert logic for both POST and PUT.
* Smart fallback for new devices.
* Reset endpoints and default values.

#### **Logging Service (Port 8081)**

* Persists readings with computed `isOverLimit`.
* Full and critical-only reading history.

---

### 🗄️ **Databases**

* **PostgreSQL – Control**: device configuration
* **PostgreSQL – Logging**: temperature readings

---

### **Mobile App (React Native / Expo)**

All requests go exclusively through the API Gateway using `EXPO_PUBLIC_API_BASE_URL`.

---

## 🛠️ **Tech Stack**

### **Backend**

* Java 17
* Spring Boot 3 (Web, Data JPA, Validation)
* Spring Cloud Gateway (WebFlux)
* PostgreSQL
* Hibernate / JPA
* Lombok
* Docker & Docker Compose
* `spring-dotenv` for environment variables

### **Mobile**

* React Native (Expo)
* TypeScript
* twrnc (Tailwind-like utilities)
* Fetch API

### **Embedded**

* ESP32
* C firmware
* HTTP client for configuration + logging APIs

---

# 🧩 **Microservices**

## **1. Control Service**

Manages configuration for each device (`deviceId`).

### **DeviceConfig Fields**

* `deviceId`
* `temperatureLimit`
* `highToleranceEnabled`
* `highToleranceReason`
* `effectiveTemperatureLimit`
* `readingIntervalMs`
* `createdAt`, `updatedAt`

### **Core Behaviors**

* **Semantic Upsert**

  * `POST /api/config`
  * `PUT /api/config`
    Both use a shared internal upsert method.

* **Reset to Defaults**
  `PUT /api/config/{deviceId}/reset`

* **Intelligent Fallback**
  Returns in-memory defaults for unknown devices.

---

## **2. Logging Service**

Stores periodic temperature readings from ESP32.

### **SensorReading Fields**

* `id`
* `deviceId`
* `temperature`
* `temperatureLimit`
* `isOverLimit`
* `timestamp`

### **Core Behaviors**

* `POST /api/readings`
  Minimal response to optimize embedded bandwidth.

* `GET /api/readings/{deviceId}`

* `GET /api/readings/{deviceId}/criticals`

---

## **3. API Gateway**

Unified entry point (`:8080`).
Routes:

* `/api/readings/**` → `:8081`
* `/api/config/**` → `:8082`

---

# 📱 **Mobile App Structure**

Located at: `mobile/PrevenfireIot`

## **Tabs**

---

### **Configs Tab**

* Device ID input with debounce
* Auto-fetch config
* Fields:

  * `temperatureLimit`
  * `readingIntervalSeconds`
  * `highToleranceEnabled`
  * `highToleranceReason`
* Actions:

  * Create/Update
  * Reset configuration

---

### **Readings Tab**

* Debounced device ID
* "Critical only" filter
* List:

  * Timestamp
  * Temperature + applied limit
  * Critical highlight
* Emergency call (193)

---

# 🧪 **Services (Mobile)**

### **ConfigService.ts**

* GET `/api/config/{deviceId}`
* POST `/api/config`
* PUT `/api/config`
* PUT `/api/config/{deviceId}/reset`

### **ReadingsService.ts**

* GET `/api/readings/{deviceId}`
* GET `/api/readings/{deviceId}/criticals`

---

# 🔌 **Embedded (ESP32) Overview**

### Responsibilities

* Fetch configuration from Control Service
* Apply limits and intervals
* Read temperature continuously
* Activate actuator (LED/buzzer/alarm) when over limit
* Send readings via Gateway:
  `POST /api/readings`

---

# ▶️ **Running the Project**

## **Prerequisites**

* Docker & Docker Compose
* Java 17 + Maven
* Node.js
* Expo CLI (or `npx expo`)

---

## **1. Start Databases**

```bash
docker compose up -d
```

---

## **2. Start Backend**

🧩 **Environment Setup (.env)**

Before running the backend, make sure your environment variables are correctly configured.

Because each Spring Boot microservice runs independently (especially when started via Maven), **they do not read the `.env` file located at the project root**.
IntelliJ automatically merges environment context during execution, but Maven does **not** — therefore **each service must contain its own `.env` file**.

### **a. Create the root `.env`**

In the project root you will find:

* `.env.example`

Edit the `.env.example` with your own database credentials and then rename it to `.env`.

### **b. Copy the `.env` into each microservice**

Copy the **root `.env`** into:

```
backend/control-service/.env
backend/logging-service/.env
```

> ⚠️ **Important:**
> Without these copies, the services will not load database credentials when executed via Maven, even though IntelliJ might run them correctly.

### **c. Run Applications**

```bash
# API Gateway (8080)
cd backend/api-gateway
./mvnw spring-boot:run

# Control Service (8082)
cd backend/control-service
./mvnw spring-boot:run

# Logging Service (8081)
cd backend/logging-service
./mvnw spring-boot:run
```

---

## **3. Start Mobile App**

Inside `mobile/PrevenfireIot`:

```bash
npm install
```

Set environment variable:

```
EXPO_PUBLIC_API_BASE_URL=http://YOUR_LOCAL_IP:8080
```

Start:

```
npx expo start --port 8085
```
or
```
npx expo start --tunnel --port 8085 -c
```

---

# 📘 **API Summary**

### **Control Service**

* POST `/api/config`
* PUT `/api/config`
* GET `/api/config/{deviceId}`
* PUT `/api/config/{deviceId}/reset`
* DELETE `/api/config/{deviceId}`

### **Logging Service**

* POST `/api/readings`
* GET `/api/readings/{deviceId}`
* GET `/api/readings/{deviceId}/criticals`

All requests via API Gateway:
`http://YOUR_LOCAL_IP:8080`

---

# 🖼️ **Screenshots & Demo**

* Device Config: ``
* Readings Monitoring: ``

Demo video (ESP32 + Backend + App):
``

---

# 📌 **Roadmap**

### Backend

* Pagination
* List distinct devices
* Aggregate insights

### Mobile

* Device selector
* Favorites / recent
* Alternative UI themes

### Embedded

* Reconnection logic
* Local logs

---

# 📄 **License**

This project is a proof of concept for robust temperature monitoring and alerting systems.
Feel free to adapt it to your environment (cold storage, pools, data centers, etc.) while maintaining best practices for security and scalability.
