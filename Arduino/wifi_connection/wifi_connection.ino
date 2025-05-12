#include <WiFi.h>

// Replace with your Wi-Fi network credentials
const char* ssid = "ESP32";
const char* password = "123456KRNM";

// Function to connect to Wi-Fi
void connectToWiFi() {
  Serial.print("Connecting to Wi-Fi: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);  // Start connecting to Wi-Fi

  // Wait until connected
  int retryCount = 0;
  while (WiFi.status() != WL_CONNECTED && retryCount < 10) {
    delay(1000);
    Serial.print(".");
    retryCount++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWi-Fi Connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nFailed to connect to Wi-Fi. Please check your credentials.");
  }
}

void setup() {
  Serial.begin(115200);  // Initialize Serial Monitor
  connectToWiFi();       // Attempt Wi-Fi connection
}

void loop() {
  // Keep checking Wi-Fi connection status
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi disconnected. Reconnecting...");
    connectToWiFi();
  }
  delay(10000);  // Check every 10 seconds
}
