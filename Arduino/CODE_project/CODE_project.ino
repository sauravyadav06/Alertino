#include <WiFi.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <arduinoFFT.h>
#include <ArduinoMqttClient.h>
#include <ArduinoJson.h>

// Wi-Fi Credentials
const char* ssid = "Infinix NOTE 40 Pro BMW"; // Replace with your SSID
const char* password = "alok5150"; // Replace with your Password

//MQTT Credentials
const char* mqtt_server = "broker.emqx.io";
const int mqtt_port = 1883; 
const char* mqtt_topic = "data/sensor/ESP32";
const char* mqtt_userName = "admin";
const char* mqtt_password = "admin123";

// DHT11 Configuration
#define DHTPIN 4          // Pin connected to DHT11
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

// Vibration Sensor Configuration
#define VIBRATION_PIN 32   // Ensure the correct GPIO pin is set
#define VIBRATION_THRESHOLD 100

// ACS712 Current Sensor Configuration
#define CURRENT_SENSOR_PIN 34
#define VOLTAGE_REF 3.3
#define ADC_RESOLUTION 4095
float sensitivity = 0.185; // ACS712 sensitivity
float zeroCurrentOffset = 2.5;

WiFiClient wifiClient;
MqttClient mqttClient(wifiClient);

void connectToWiFi() {
  Serial.print("Connecting to Wi-Fi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int retry_count = 0;
  while (WiFi.status() != WL_CONNECTED && retry_count < 10) {
    delay(1000);
    Serial.print(".");
    retry_count++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWi-Fi connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nFailed to connect to Wi-Fi");
  }
  // Automatically reconnect Wi-Fi if lost
    WiFi.onEvent([](WiFiEvent_t event, WiFiEventInfo_t info) {
        Serial.println("Wi-Fi Disconnected! Reconnecting...");
        WiFi.begin(ssid, password);
    }, WiFiEvent_t::ARDUINO_EVENT_WIFI_STA_DISCONNECTED);
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  // Initialize Wi-Fi
  connectToWiFi();

  // Initialize DHT11
  dht.begin();

  // Initialize vibration sensor pin
  pinMode(VIBRATION_PIN, INPUT);

  Serial.println("All Sensors Initialized!");
}

void loop() {
  // Create a JSON document
    StaticJsonDocument<200> jsonDoc;
  // === Read and Display DHT11 Sensor Data ===
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("Failed to read from DHT11 sensor!");
  } else {
    Serial.print("Temperature: ");
    Serial.print(temperature);
    Serial.println(" °C");
    Serial.print("Humidity: ");
    Serial.print(humidity);
    Serial.println(" %");
    String temperatureStr = String(temperature) + " °C";
    String humidityStr = String(humidity) + " %";
    jsonDoc["temperature"] = temperatureStr;
    jsonDoc["humidity"] = humidityStr;
  }

  // === Read and Display Vibration Sensor Data ===
  int vibration = analogRead(VIBRATION_PIN);
  Serial.print("Vibration Sensor Reading: ");
  Serial.println(vibration);
  jsonDoc["vibration"] = vibration;

  if (vibration > VIBRATION_THRESHOLD)
  {
    Serial.println("ALERT! Vibration Threshold Exceeded!");
  }

  // === Read and Display ACS712 Current Sensor Data ===
  int rawADC = analogRead(CURRENT_SENSOR_PIN);
  float voltage = (rawADC / (float)ADC_RESOLUTION) * VOLTAGE_REF;
  float current = (voltage - zeroCurrentOffset) / sensitivity;
  String currentStr = String(current) + " A";
  String voltageStr = String(voltage) + " V";
  jsonDoc["current"] = currentStr;
  jsonDoc["voltage"] = voltageStr;

  Serial.print("Current: ");
  Serial.print(current, 3);
  Serial.println(" A");

 // Convert JSON to a string
    String jsonString;
    serializeJson(jsonDoc, jsonString);
    Serial.println(jsonString);

  if (!mqttClient.connected()) {
        reconnect();
        return;
    }

     mqttClient.beginMessage(mqtt_topic);
     mqttClient.print(jsonString);
    mqttClient.endMessage();
    
    mqttClient.poll();

  delay(2000);  // Delay for next reading
}
void reconnect() {
    while (!mqttClient.connected()) {
        Serial.print("Attempting MQTT connection...");
         // Set username and password
        mqttClient.setUsernamePassword(mqtt_userName, mqtt_password);
        if (mqttClient.connect(mqtt_server, mqtt_port)) {
            Serial.println("connected");
        } else {
            Serial.print("failed, rc=");
            Serial.println("MQTT Disconnected!");
            Serial.println(" try again in 5 seconds");
            delay(5000);
        }
    }
}
