package com.example.mqttalert

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.eclipse.paho.mqttv5.client.*
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var context : Context = this
    private lateinit var mqttClient: MqttAsyncClient
    private var isActivityForeground = false

    private lateinit var welcomeTextView: TextView
    private lateinit var connectionStatusText: TextView
    private lateinit var tempText: TextView
    private lateinit var humidityText: TextView
    private lateinit var vibrationText: TextView
    private lateinit var currentText: TextView
    private lateinit var voltageText: TextView
    private lateinit var fabDashboard: FloatingActionButton

    private val serverUri = "tcp://broker.emqx.io:1883"
    private val topic = "data/sensor/ESP32"
    private val clientId = "AndroidClient_${System.currentTimeMillis()}"
    private val TEMPERATURE_THRESHOLD = 50.0
    private val VIBRATION_THRESHOLD = 80.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcomeTextView = findViewById(R.id.welcomeTextView)
        connectionStatusText = findViewById(R.id.connectionStatusText)
        tempText = findViewById(R.id.tempText)
        humidityText = findViewById(R.id.humidityText)
        vibrationText = findViewById(R.id.vibrationText)
        currentText = findViewById(R.id.currentText)
        voltageText = findViewById(R.id.voltageText)
        fabDashboard = findViewById(R.id.btnDashboard)

        // Launch DashboardActivity on FAB click
        fabDashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        val sharedPreferences = getSharedPreferences("MQTTAlertPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", "User") ?: "User"
        welcomeTextView.text = "Welcome, $username"
        Toast.makeText(this, "Welcome , $username!", Toast.LENGTH_SHORT).show()

        createNotificationChannel()
        setupMqttClient()
    }

    override fun onResume() {
        super.onResume()
        isActivityForeground = true
        if (::mqttClient.isInitialized && !mqttClient.isConnected) {
            connectMqttClient()
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            try {
                mqttClient.disconnect()
            } catch (e: MqttException) {
                Log.e("MQTT", "Error disconnecting: ${e.message}")
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun setupMqttClient() {
        try {
            mqttClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
            mqttClient.setCallback(object : MqttCallback {
                override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
                    connectionStatusText.text = "Disconnected"
                    if (isActivityForeground) connectMqttClient()
                }

                override fun mqttErrorOccurred(exception: MqttException?) {
                    Log.e("MQTT", "MQTT Error: ${exception?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic != this@MainActivity.topic) return
                    Log.e("MQTTRES", message.toString())
                    message?.let { processMessage(it.toString()) }
                }

                override fun deliveryComplete(token: IMqttToken?) {}
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    subscribeToTopic()
                }

                override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {}
            })
            connectMqttClient()
        } catch (e: MqttException) {
            Log.e("MQTT", "Setup error: ${e.message}")
        }
    }

    private fun connectMqttClient() {
        val options = MqttConnectionOptions().apply {
            isCleanStart = true
            isAutomaticReconnect = true
            userName = "admin"
            password = "admin123".encodeToByteArray()
            connectionTimeout = 10
            keepAliveInterval = 20
        }

        try {
            mqttClient.connect(options, null, object : MqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    connectionStatusText.text = "Connected to your Device"
                }

                override fun onFailure(token: IMqttToken?, exception: Throwable?) {
                    connectionStatusText.text = "Failed to connect"
                    if (isActivityForeground) {
                        Handler(mainLooper).postDelayed({
                            connectMqttClient()
                        }, 5000)
                    }
                }
            })
        } catch (e: MqttException) {
            Log.e("MQTT", "Connection error: ${e.message}")
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttClient.subscribe(topic, 1, null, object : MqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    Log.d("MQTT", "Subscribed to $topic")
                }

                override fun onFailure(token: IMqttToken?, exception: Throwable?) {
                    if (isActivityForeground) {
                        Handler(mainLooper).postDelayed({
                            subscribeToTopic()
                        }, 5000)
                    }
                }
            })
        } catch (e: MqttException) {
            Log.e("MQTT", "Subscription error: ${e.message}")
        }
    }

    private fun processMessage(jsonString: String) {
        try {
            val json = JSONObject(jsonString)

            val temperature = json.optString("temperature").replace("[^\\d.-]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val humidity = json.optString("humidity", "N/A")
            val vibration = json.optDouble("vibration", 0.0)
            val current = json.optString("current", "N/A")
            val voltage = json.optString("voltage", "N/A")

            if (isActivityForeground) {
                runOnUiThread {
                    tempText.text = "Temperature: $temperature °C"
                    humidityText.text = "Humidity: $humidity"
                    vibrationText.text = "Vibration: $vibration"
                    currentText.text = "Current: $current"
                    voltageText.text = "Voltage: $voltage"
                }
            }

            // Broadcast sensor data to DashboardActivity
            val intent = Intent("com.example.mqttalert.SENSOR_DATA").apply {
                putExtra("sensorData", jsonString)
            }
            sendBroadcast(intent)

            val isHighTemp = temperature > TEMPERATURE_THRESHOLD
            val isHighVibration = vibration > VIBRATION_THRESHOLD

            when {
                isHighTemp && isHighVibration -> sendNotification(
                    "Critical Alert!", "Both Temperature and Vibration are above safe limits!"
                )
                isHighTemp -> sendNotification("High Temperature!", "Temperature exceeded $TEMPERATURE_THRESHOLD°C")
                isHighVibration -> sendNotification("High Vibration!", "Vibration exceeded $VIBRATION_THRESHOLD units")
            }

        } catch (e: Exception) {
            Log.e("MQTT", "Parsing error: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sensor_alerts_channel", "Sensor Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for sensor alert thresholds"
            }

            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, "sensor_alerts_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}