⚡ Alertino – IoT-Based Predictive Maintenance
Alertino is a smart IoT-based real-time monitoring system built with Android (Kotlin), ESP32/Arduino, and MQTT to detect anomalies in industrial and home machinery like coolers, motors, or fans.

It tracks environmental and electrical parameters — temperature, humidity, vibration, current, and voltage — and sends real-time alerts when thresholds are breached. The data is visualized on a mobile app dashboard with dynamic charts and cards.

🛠️ Key Features
Real-Time Monitoring – Continuously tracks sensor data.

Threshold-Based Alerts – Sends push notifications on anomaly detection.

Visual Analytics Dashboard – Displays real-time data with cards and charts.

Lightweight Communication – Uses MQTT for efficient message delivery.

Offline-to-Online Sync – Ensures reliable data logging during network loss.

🧰 Technologies Used
Android App: Kotlin, Android Studio, MQTT Client

Microcontroller: ESP32 using Arduino IDE

Communication: MQTT via EMQX / Mosquitto Broker

UI Components: CardViews, Charts, Gauges, RecyclerView, etc.

📟 Sensors Used
Sensor	Purpose	GPIO Pin
DHT11	Measures temperature & humidity	GPIO 4
Vibration (Analog)	Detects abnormal vibrations	GPIO 32
ACS712	Measures current and voltage	GPIO 34

📲 Android App Highlights
Displays real-time sensor values

Push notifications for threshold violations

Dynamic dashboard with cards and charts

Clean, responsive UI

🔌 How to Use
🔧 Arduino/ESP32 Setup
Connect sensors as per GPIO mapping (see above).

Flash the Arduino sketch to your ESP32 board.

Set Wi-Fi credentials and MQTT broker details in the code.

🌐 MQTT Broker
Use public broker broker.emqx.io or host your own (e.g., Mosquitto).

Subscribe to topic: data/sensor/ESP32

📱 Android App
Open project in Android Studio.

Update the MQTT broker IP in the app’s configuration file.

Run the app on a physical Android device.

📊 Use Cases
🏭 Motor / Cooler Health Monitoring

🧪 Industrial Diagnostics

🔧 Predictive Maintenance

🏠 Smart Home / Factory Automation

📬 Contact
Need help or want to collaborate?
📧 sauravyadav5150@example.com
