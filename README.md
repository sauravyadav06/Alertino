IoT-Based Real-Time Monitoring System
A Smart Monitoring System built with Android (Kotlin), ESP32/Arduino, and MQTT to detect anomalies in machinery like coolers or motors. It monitors environmental and electrical parameters such as temperature, humidity, vibration, current, and voltage, sending real-time alerts via MQTT when thresholds are exceeded. Data is displayed in a mobile app and logged for visualization on an analytics dashboard.
🛠️ Key Features

Real-Time Monitoring: Continuously tracks sensor data.
Threshold-Based Alerts: Push notifications for anomalies.
Visual Analytics Dashboard: Displays data with charts and cards.
Lightweight Communication: Uses MQTT for fast, efficient data transfer.
Offline-to-Online Sync: Ensures data consistency across connectivity states.

🧰 Technologies Used

Android App: Kotlin, Android Studio, MQTT client
Microcontroller: ESP32 with Arduino IDE
Communication: MQTT (via EMQX/Mosquitto Broker)
UI Elements: CardViews,Charts, Gauges,etc

📟 Sensors Used

The system monitors environmental and electrical parameters using the following sensors:
DHT11
Purpose: Measures temperature and humidity
GPIO Pin: GPIO 4

Analog Vibration
Purpose: Detects abnormal vibrations
GPIO Pin: GPIO 32

ACS712 Current
Purpose: Measures current and voltage
GPIO Pin: GPIO 34


📲 Android App Highlights

Displays real-time sensor data
Sends push notifications for threshold breaches
Features dynamic charts and cards for clear insights
Clean, responsive design

🔌 How to Use
1. Arduino/ESP32 Setup

Connect sensors as per the GPIO mapping above.
Flash the Arduino sketch to your ESP32.
Configure Wi-Fi and MQTT broker settings in the sketch.

2. MQTT Broker

Use broker.emqx.io or set up your own broker (e.g., Mosquitto).
Subscribe to the topic: data/sensor/ESP32

3. Android App

Open the project in Android Studio.
Update the MQTT broker IP in the app configuration.
Run the app on a physical Android device.

📊 Use Cases

Motor/Cooler Health Monitoring
Industrial Diagnostics
Predictive Maintenance
Smart Home or Factory Automation

📬 Contact
Need help or want to collaborate? Reach out at sauravyadav5150@example.com
