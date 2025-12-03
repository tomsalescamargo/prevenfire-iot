#include <WiFi.h>
#include <WiFiClient.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <DHT.h>

#define DHTPIN 27  // DHT11 pin (GPIO27)
#define DHTTYPE DHT11

#define LED_PIN 25  // LED pin (GPIO25)

const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// Backend configuration
const char* API_GATEWAY_URL = "http://YOUR_IP_ADDRESS:8080";
const char* DEVICE_ID = "YOUR_DEVICE_ID";

// Temperature configuration placeholders
float effectiveTemperatureLimit = 30.0;
int readingIntervalMs = 30000;
bool highToleranceEnabled = false;

// Timing control
unsigned long lastCycleTime = 0;

DHT dht(DHTPIN, DHTTYPE);
HTTPClient http;
WiFiClient client;

void setup() {
  Serial.begin(115200);
  dht.begin();
  pinMode(LED_PIN, OUTPUT);
  Serial.println("Initializing temperature monitoring system...");
  delay(3000);

  // Connect to Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected successfully. IP: ");
  Serial.println(WiFi.localIP());

  // Initial cycle: fetch config + first reading
  fullSensorCycle();
  lastCycleTime = millis();
}

void loop() {
  unsigned long currentTime = millis();

  // Full cycle every readingIntervalMs: fetch config + read sensor + send reading
  if (currentTime - lastCycleTime >= (unsigned long)readingIntervalMs) {
    fullSensorCycle();
    lastCycleTime = currentTime;
  }

  delay(100);
}

void fullSensorCycle() {
  // 1. Always fetch fresh config first
  fetchConfiguration();
  delay(500);

  // 2. Read sensor with updated config
  float temperature = dht.readTemperature();
  if (!isnan(temperature)) {
    // 3. Control LED
    bool isOverLimit = temperature > effectiveTemperatureLimit;
    digitalWrite(LED_PIN, isOverLimit ? HIGH : LOW);

    // 4. Send reading to backend
    sendTemperatureReading(temperature, effectiveTemperatureLimit);

    // 5. Display on Serial
    Serial.print("Temp: ");
    Serial.print(temperature);
    Serial.print("°C | Effective Limit: ");
    Serial.print(effectiveTemperatureLimit);
    Serial.print("°C | Over limit: ");
    Serial.println(isOverLimit ? "YES" : "NO");
  } else {
    Serial.println("Failed to read DHT11 sensor");
  }
}

void fetchConfiguration() {
  if (WiFi.status() != WL_CONNECTED) return;

  http.end();
  delay(50);

  String url = String(API_GATEWAY_URL) + "/api/config/" + DEVICE_ID + "?defaultIfAbsent=true";
  http.begin(client, url);
  http.setConnectTimeout(5000);
  http.setTimeout(5000);

  int httpCode = http.GET();
  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    http.end();
    delay(100); 
  
    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, payload);

    if (!error) {
      effectiveTemperatureLimit = doc["effectiveTemperatureLimit"];
      readingIntervalMs = doc["readingIntervalMs"];
      highToleranceEnabled = doc["highToleranceEnabled"];

      Serial.print("Config updated - Effective Limit: ");
      Serial.print(effectiveTemperatureLimit);
      Serial.print("°C | Interval: ");
      Serial.print(readingIntervalMs / 1000);
      Serial.println("s");
    }
  } else {
    Serial.print("Config fetch failed (HTTP ");
    Serial.print(httpCode);
    Serial.println(")");
    http.end();
  }

  http.end();
}

void sendTemperatureReading(float temperature, float temperatureLimit) {
  if (WiFi.status() != WL_CONNECTED) return;

  http.end();
  delay(50);

  http.begin(client, String(API_GATEWAY_URL) + "/api/readings");
  http.setConnectTimeout(5000);
  http.setTimeout(5000);

  DynamicJsonDocument doc(256);
  doc["deviceId"] = DEVICE_ID;
  doc["temperature"] = temperature;
  doc["temperatureLimit"] = temperatureLimit;

  String payload;
  serializeJson(doc, payload);

  http.addHeader("Content-Type", "application/json");
  int httpCode = http.POST(payload);

  if (httpCode == HTTP_CODE_OK || httpCode == 201) {
    Serial.println("Reading sent successfully");
  } else {
    Serial.print("Send failed (HTTP ");
    Serial.print(httpCode);
    Serial.println(")");
  }
  
  http.end();
  delay(50);
}