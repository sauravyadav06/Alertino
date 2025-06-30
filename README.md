# ⚡ Alertino – IoT-Based Predictive Maintenance

**Alertino** is a smart IoT-based real-time monitoring system built with **Android (Kotlin)**, **ESP32/Arduino**, and **MQTT** to detect anomalies in machinery like coolers, motors, or fans.

It monitors **temperature**, **humidity**, **vibration**, **current**, and **voltage**, sending real-time alerts when thresholds are exceeded. Data is visualized in a mobile app dashboard with dynamic cards and charts.

---

## 🛠️ Key Features

- **Real-Time Monitoring** – Continuously tracks sensor data.
- **Threshold-Based Alerts** – Sends push notifications on anomaly detection.
- **Visual Analytics Dashboard** – Real-time display using charts and cards.
- **Lightweight Communication** – MQTT-based efficient messaging.
- **Offline-to-Online Sync** – Ensures reliable data logging during network outages.

---

## 🧰 Technologies Used

- **Android App:** Kotlin, Android Studio, MQTT Client
- **Microcontroller:** ESP32 using Arduino IDE
- **Communication:** MQTT via EMQX / Mosquitto Broker
- **UI Components:** CardViews, Charts, Gauges, RecyclerView, etc.

---

## 📟 Sensors Used

| Sensor             | Purpose                          | GPIO Pin |
|--------------------|----------------------------------|----------|
| **DHT11**          | Measures temperature & humidity  | GPIO 4   |
| **Analog Vibration** | Detects abnormal vibrations     | GPIO 32  |
| **ACS712**         | Measures current and voltage     | GPIO 34  |

---

## 📲 Android App Highlights

- Displays real-time sensor values
- Push notifications for threshold breaches
- Dynamic cards and charts for clear insights
- Clean and responsive design

---

## 🔌 How to Use

### 1. Arduino/ESP32 Setup

- Connect sensors as per GPIO mapping above.
- Flash the Arduino sketch to your ESP32 board.
- Configure Wi-Fi and MQTT broker in the code.

### 2. MQTT Broker

- Use `broker.emqx.io` or set up your own (e.g., Mosquitto).
- Subscribe to topic: `data/sensor/ESP32`

### 3. Android App

- Open the project in Android Studio.
- Update the MQTT broker IP in the config file.
- Build and run the app on a physical Android device.

---

## 📊 Use Cases

- 🏭 Motor / Cooler Health Monitoring  
- 🧪 Industrial Diagnostics  
- 🔧 Predictive Maintenance  
- 🏠 Smart Home or Factory Automation

---

## 📬 Contact

Need help or want to collaborate?  
📧 sauravyadav5150@example.com
