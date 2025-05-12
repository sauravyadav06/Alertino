#include <Adafruit_Sensor.h>

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>

// Pin Definitions
#define DHT_PIN 4           // DHT11 sensor data pin connected to GPIO4
#define DHT_TYPE DHT11      // Type of DHT sensor (DHT11)
#define VIBRATION_PIN 34    // SW-450 vibration sensor connected to GPIO34 (analog pin)
#define CURRENT_SENSOR_PIN 35 // ACS712 current sensor connected to GPIO35 (analog pin)

// DHT Sensor Initialization
DHT dht(DHT_PIN, DHT_TYPE);

// Constants for Sensors
const float ACS712_SENSITIVITY = 0.185; // Sensitivity of ACS712 (for 5A version) in V/A
const int ADC_RESOLUTION = 4096;        // ESP32 ADC resolution (12-bit)
const float REF_VOLTAGE = 3.3;          // ESP32 reference voltage

void setup() {
  Serial.begin(115200);      // Start Serial Communication
  dht.begin();               // Initialize the DHT sensor
  pinMode(VIBRATION_PIN, INPUT); // Set the vibration sensor pin as input
  pinMode(CURRENT_SENSOR_PIN, INPUT); // Set current sensor pin as input
  
  Serial.println("HVAC Predictive Maintenance System");
}

void loop() {
  // Read temperature and humidity from DHT11
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  // Read vibration sensor value
  int vibrationValue = analogRead(VIBRATION_PIN);

  // Read current sensor value and calculate current
  int rawCurrentValue = analogRead(CURRENT_SENSOR_PIN);
  float voltage = (rawCurrentValue / (float)ADC_RESOLUTION) * REF_VOLTAGE;
  float current = (voltage - (REF_VOLTAGE / 2)) / ACS712_SENSITIVITY;

  // Check for DHT sensor errors
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("Failed to read from DHT sensor!");
  } else {
    Serial.print("Temperature: ");
    Serial.print(temperature);
    Serial.print(" °C, Humidity: ");
    Serial.print(humidity);
    Serial.println(" %");
  }

  // Display vibration value
  Serial.print("Vibration Value: ");
  Serial.println(vibrationValue);

  // Display current value
  Serial.print("Current: ");
  Serial.print(current);
  Serial.println(" A");

  // Delay before the next reading
  delay(2000);
}
