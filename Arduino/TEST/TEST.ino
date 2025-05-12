#include <WiFi.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>

// Wi-Fi Credentials
const char* ssid = "KG";         // Replace with your SSID
const char* password = "kgaonkar05"; // Replace with your Password

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

// Connect to Wi-Fi
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
  }

  // === Read and Display Vibration Sensor Data with Averaging ===
  int totalVibration = 0;
  for (int i = 0; i < 10; i++) {
    totalVibration += analogRead(VIBRATION_PIN);
    delay(5);  // Small delay to smooth readings
  }
  int avgVibration = totalVibration / 10;

  Serial.print("Vibration Sensor Reading (Averaged): ");
  Serial.println(avgVibration);

  if (avgVibration > VIBRATION_THRESHOLD)
  {
    Serial.println("ALERT! Vibration Threshold Exceeded!");
  }

  // === Read and Display ACS712 Current Sensor Data ===
  int rawADC = analogRead(CURRENT_SENSOR_PIN);
  float voltage = (rawADC / (float)ADC_RESOLUTION) * VOLTAGE_REF;
  float current = (voltage - zeroCurrentOffset) / sensitivity;

  Serial.print("Current: ");
  Serial.print(current, 3);
  Serial.println(" A");

  delay(2000);  // Delay for next reading
}
