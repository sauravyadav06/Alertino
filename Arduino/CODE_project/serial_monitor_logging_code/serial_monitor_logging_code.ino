#include <Adafruit_Sensor.h>
#include <DHT.h>

// Sensor Pins
#define DHTPIN 4
#define DHTTYPE DHT11
#define VIBRATION_SENSOR_PIN 32
#define CURRENT_SENSOR_PIN 34

// Initialize DHT Sensor
DHT dht(DHTPIN, DHTTYPE);

// Constants for ACS712 Current Sensor
#define VOLTAGE_REF 3.3
#define ADC_RESOLUTION 4095
float sensitivity = 0.185; 
float zeroCurrentOffset = 2.5;

void setup() {
  Serial.begin(115200);  // Start Serial Monitor with baud rate 115200
  dht.begin();           // Start DHT11 sensor
  pinMode(VIBRATION_SENSOR_PIN, INPUT); // Vibration sensor as input

  Serial.println("Timestamp,Temperature,Humidity,Vibration,Current"); // CSV Header
}

void loop() {
  // === Read Sensor Data ===
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();
  int vibrationValue = analogRead(VIBRATION_SENSOR_PIN);
  int rawADC = analogRead(CURRENT_SENSOR_PIN);
  float voltage = (rawADC / (float)ADC_RESOLUTION) * VOLTAGE_REF;
  float current = (voltage - zeroCurrentOffset) / sensitivity;

  // === Generate Timestamp ===
  unsigned long timestamp = millis(); // Time in milliseconds since boot

  // === Print Data in CSV Format ===
  Serial.print(timestamp);
  Serial.print(",");
  Serial.print(temperature);
  Serial.print(",");
  Serial.print(humidity);
  Serial.print(",");
  Serial.print(vibrationValue);
  Serial.print(",");
  Serial.println(current);

  delay(2000); // Wait 2 seconds before next reading
}
