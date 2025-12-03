# **PrevenFire IoT – Temperature Monitoring & Alerting Platform 🔥📡**

## 1. Overview

PrevenFire IoT is a complete proof-of-concept platform for real-time temperature monitoring and alerting. It integrates **Java 17**, **Spring Boot microservices**, an **API Gateway**, **PostgreSQL**, **Docker**, and a **React Native (Expo)** mobile app—fully connected to an **ESP32** embedded device.

This project was built with a strong focus on **clean architecture**, **separation of concerns**, **scalability**, **solid backend design**, and **mobile best practices** (debounce, loading states, UX feedback). Although visually aligned with fire-risk scenarios (kitchens, offices, warehouses), the same structure supports cold rooms, server racks, greenhouses, heated pools, and other temperature-sensitive environments.

---

## 2. Features ⚙️

### 2.1 Centralized IoT Device Configuration

* Configure temperature limits, reading intervals, and high-tolerance mode directly from the mobile app.
* Backend automatically computes `effectiveTemperatureLimit` when tolerance mode is enabled.

### 2.2 Real-Time Sensor Logging

* ESP32 sends periodic readings alongside the applied limit.
* Full history per device and dedicated filtering for critical (over-limit) events.

### 2.3 Mobile App (React Native / Expo)

* Configuration screens for device ID, limit, interval, and tolerance.
* Real-time reading list with visual indicators for critical states.
* Emergency shortcut (193).
* UX optimized with debounce and status messages.

### 2.4 Microservices + API Gateway

* **Control Service** → device configuration
* **Logging Service** → temperature readings history
* **API Gateway** → unified entry point for mobile and embedded requests

### 2.5 Containerized Infrastructure 🐳

* Two independent PostgreSQL databases.
* Docker Compose orchestration with isolated networking.

---

## 3. Architecture 🏗️

### 3.1 Embedded Device (ESP32)

* Reads temperature.
* Retrieves configuration from Control Service.
* Sends periodic readings to Logging Service.

### 3.2 Backend Services (Java / Spring Boot)

#### API Gateway (Port 8080)

Routes:

* `/api/config/**` → Control Service
* `/api/readings/**` → Logging Service

#### Control Service (Port 8082)

* CRUD for device configurations.
* Upsert logic shared by POST and PUT.
* Intelligent fallback for new devices.
* Reset endpoints and default values.

#### Logging Service (Port 8081)

* Stores readings with computed `isOverLimit`.
* Full and critical-only reading history.

### 3.3 Databases

* **PostgreSQL – Control**: device configuration
* **PostgreSQL – Logging**: temperature readings

### 3.4 Mobile App

All requests go through the API Gateway using the `EXPO_PUBLIC_API_BASE_URL` environment variable.

---

## 4. Tech Stack 💻

### 4.1 Backend

* Java 17
* Spring Boot 3 (Web, Data JPA, Validation)
* Spring Cloud Gateway (WebFlux)
* PostgreSQL
* Hibernate / JPA
* Lombok
* Docker & Docker Compose

### 4.2 Mobile

* React Native (Expo)
* TypeScript
* twrnc (Tailwind-style utilities)
* Fetch API

### 4.3 Embedded

* ESP32
* C firmware
* HTTP client for config + logging

---

## 5. API Summary 🔗

### 5.1 Control Service

* POST `/api/config`
* PUT `/api/config`
* GET `/api/config/{deviceId}`
* PUT `/api/config/{deviceId}/reset`
* DELETE `/api/config/{deviceId}`

### 5.2 Logging Service

* POST `/api/readings`
* GET `/api/readings/{deviceId}`
* GET `/api/readings/{deviceId}/criticals`
  (Through API Gateway `:8080`)

---

## 6. Running the Project ▶️

### 6.1 Prerequisites

* Docker & Docker Compose
* Java 17 + Maven
* Node.js
* Expo CLI (or `npx expo`)

### 6.2 Start Databases

```bash
docker compose up -d
```

### 6.3 Start Backend

Because each Spring Boot microservice runs independently, they **do not inherit** the root `.env` when started via Maven. IntelliJ merges environment contexts automatically, but Maven does not—so each service must contain its own `.env`.

Steps:

1. Edit `.env.example` in the project root and rename it to `.env`.

2. Copy this `.env` into:

   ```
   backend/control-service/.env
   backend/logging-service/.env
   ```

3. Start services:

```bash
# API Gateway
cd backend/api-gateway
./mvnw spring-boot:run

# Control Service
cd backend/control-service
./mvnw spring-boot:run

# Logging Service
cd backend/logging-service
./mvnw spring-boot:run
```

### 6.4 Start Mobile App

Inside `mobile/PrevenfireIot`:

```bash
npm install
```

Set environment variable:

```bash
EXPO_PUBLIC_API_BASE_URL=http://YOUR_LOCAL_IP:8080
```

Run:

```bash
npx expo start --port 8085
```

Or:

```bash
npx expo start --tunnel --port 8085 -c
```

---

## 7. Mobile App Structure 📲

### 7.1 Configs Tab

* Debounced device ID input

* Auto-fetch config

* Fields:

  * `temperatureLimit`
  * `readingIntervalSeconds`
  * `highToleranceEnabled`
  * `highToleranceReason`

* Actions:

  * Create/Update
  * Reset configuration

### 7.2 Readings Tab

* Debounced device ID

* “Critical only” filter

* List:

  * Timestamp
  * Temperature + applied limit
  * Critical highlight

* Emergency call shortcut (193) 🚨

### 7.3 Mobile Services

#### ConfigService.ts

* GET `/api/config/{deviceId}`
* POST `/api/config`
* PUT `/api/config`
* PUT `/api/config/{deviceId}/reset`

#### ReadingsService.ts

* GET `/api/readings/{deviceId}`
* GET `/api/readings/{deviceId}/criticals`

---

## 8. Embedded (ESP32) Overview 🔧

Responsibilities:

* Fetch configuration from Control Service
* Apply limits and intervals
* Continuously read temperature
* Trigger actuator (LED/buzzer/alarm) when over limit
* Send readings via Gateway: `POST /api/readings`

---

## 9. Screenshots & Demo 🎥

Below are some screenshots of the PrevenFire IoT mobile application used in this proof-of-concept.
For this POC, I used **a single embedded device (ESP32)**.
However, the entire architecture fully supports **multiple devices simultaneously**, as long as each one uses its own `deviceId`.

---

### 9.1 Home Screen

<img src="https://github.com/user-attachments/assets/51dc0ea1-b41b-4bd1-a02f-1902edfec7fc" width="300" />

---

### 9.2 Device Configuration

<img src="https://github.com/user-attachments/assets/d5da4241-05b7-4b6b-97ed-509c38f2a971" width="300" />

---

### 9.3 Real-Time Monitoring

<img src="https://github.com/user-attachments/assets/a02ebcc0-236d-4ad1-9e7b-a9e80c472958" width="300" />

---

### 9.4 Demonstration Video
The video below shows the full workflow of the platform in action — from configuring settings in the app, through communication with the ESP32, to the light triggered when the temperature exceeds the threshold:
🎞️ **YouTube – Full Demo:** [https://youtube.com/shorts/zkysfFy7sE8?si=CHGJWQq9r-BO13Zx](https://youtube.com/shorts/zkysfFy7sE8?si=CHGJWQq9r-BO13Zx)

---

## 10. Roadmap 🧭

### Backend

* Pagination
* List distinct devices
* Aggregate insights

### Mobile

* Device selector
* Favorites / recent
* Theme options

### Embedded

* Reconnection logic
* Local logs

---

## 11. License 📄

This is a proof-of-concept platform for temperature monitoring and alerting. You may adapt it for environments such as cold storage, pools, or data centers while maintaining good security and scalability practices.
