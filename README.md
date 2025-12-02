# **PrevenFire IoT – Temperature Monitoring & Alerting Platform**

PrevenFire IoT is a complete proof-of-concept platform for real-time temperature monitoring and alerting.
It integrates **Java 17**, **Spring Boot microservices**, an **API Gateway**, **PostgreSQL**, **Docker**, and a **React Native (Expo)** mobile app—fully connected to an **ESP32** embedded device.

This project was built with a strong focus on **clean architecture**, **separation of concerns**, **scalability**, **solid backend design**, and **mobile best practices** (debounce, loading states, UX feedback).
Although visually aligned with fire-risk scenarios (kitchens, offices, warehouses), the same structure supports cold rooms, server racks, greenhouses, heated pools, and other temperature-sensitive environments.

---

## 🚀 **Features**

### 🔧 **Centralized IoT Device Configuration**

* Configure temperature limits, reading intervals, and high-tolerance mode directly from the mobile app.
* Backend automatically computes `effectiveTemperatureLimit` when tolerance mode is enabled.

### 📡 **Real-Time Sensor Logging**

* ESP32 sends periodic readings alongside the applied limit.
* Full history per device and dedicated filtering for critical (over-limit) events.

### 📱 **Mobile App (React Native / Expo)**

* Configuration screens for device ID, limit, interval, and tolerance.
* Real-time reading list with visual indicators for critical states.
* Emergency shortcut (193).
* UX optimized with debounce and status messages.

### 🧩 **Microservices + API Gateway**

* **Control Service** → device configuration
* **Logging Service** → temperature readings history
* **API Gateway** → unified entry point for mobile and embedded requests

### 📦 **Containerized Infrastructure**

* Two independent PostgreSQL databases.
* Docker Compose orchestration with isolated networking.

---

## 🏗️ **Architecture Overview**

### **Embedded Device (ESP32)**

* Reads temperature.
* Retrieves configuration from Control Service.
* Sends periodic readings to Logging Service.

---

### **Backend Services (Java / Spring Boot)**

#### **API Gateway (Port 8080)**

Routes:

* `/api/config/**` → Control Service
* `/api/readings/**` → Logging Service

#### **Control Service (Port 8082)**

* CRUD for device configurations.
* Upsert logic shared by POST and PUT.
* Intelligent fallback for new devices.
* Reset endpoints and default values.

#### **Logging Service (Port 8081)**

* Stores readings with computed `isOverLimit`.
* Full and critical-only reading history.

---

## 🗄️ **Databases**

* **PostgreSQL – Control**: device configuration
* **PostgreSQL – Logging**: temperature readings

---

## 📱 **Mobile App**

All requests go through the API Gateway using the `EXPO_PUBLIC_API_BASE_URL` environment variable.

---

# 🛠️ **Tech Stack**

### **Backend**

* Java 17
* Spring Boot 3 (Web, Data JPA, Validation)
* Spring Cloud Gateway (WebFlux)
* PostgreSQL
* Hibernate / JPA
* Lombok
* Docker & Docker Compose

### **Mobile**

* React Native (Expo)
* TypeScript
* twrnc (Tailwind-style utilities)
* Fetch API

### **Embedded**

* ESP32
* C firmware
* HTTP client for config + logging

---

# 🧩 **Microservices**

## **1. Control Service**

Manages configuration for each `deviceId`.

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
  Returns a default in-memory config for unknown devices.

---

## **2. Logging Service**

Stores periodic temperature readings.

### **SensorReading Fields**

* `id`
* `deviceId`
* `temperature`
* `temperatureLimit`
* `isOverLimit`
* `timestamp`

### **Core Behaviors**

* `POST /api/readings`

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

### **Configs Tab**

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

---

### **Readings Tab**

* Debounced device ID
* “Critical only” filter
* List:

  * Timestamp
  * Temperature + applied limit
  * Critical highlight
* Emergency call shortcut (193)

---

## 🧪 **Mobile Services**

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

Responsibilities:

* Fetch configuration from Control Service
* Apply limits and intervals
* Continuously read temperature
* Trigger actuator (LED/buzzer/alarm) when over limit
* Send readings via Gateway: `POST /api/readings`

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

Because each Spring Boot microservice runs independently, they **do not inherit** the root `.env` when started via Maven.
IntelliJ merges environment contexts automatically, but Maven does not—so each service must contain its own `.env`.

### Steps:

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

Run:

```
npx expo start --port 8085
```

or:

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

(Through API Gateway `:8080`)

---

# 🖼️ **Screenshots & Demo**

* Device Config: ``
* Readings Monitoring: ``

Demo video (ESP32 + Backend + App): ``

---

# 📌 **Roadmap**

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

# 📄 **License**

This is a proof-of-concept platform for temperature monitoring and alerting.
You may adapt it for environments such as cold storage, pools, or data centers while maintaining good security and scalability practices.

