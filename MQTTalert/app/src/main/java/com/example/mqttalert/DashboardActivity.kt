
package com.example.mqttalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private val temperatureEntries = mutableListOf<BarEntry>()
    private val vibrationEntries = mutableListOf<BarEntry>()
    private val humidityEntries = mutableListOf<BarEntry>()
    private val currentEntries = mutableListOf<BarEntry>()
    private val voltageEntries = mutableListOf<BarEntry>()
    private val timestamps = mutableListOf<Long>() // Store timestamps for X-axis labels
    private var entryIndex = 0f // Incremental index for bar positions
    private val visibleXRange = 5f // Number of bar groups visible at once

    private lateinit var toggleTemperature: CheckBox
    private lateinit var toggleVibration: CheckBox
    private lateinit var toggleHumidity: CheckBox
    private lateinit var toggleCurrent: CheckBox
    private lateinit var toggleVoltage: CheckBox

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra("sensorData")?.let { jsonString ->
                processSensorData(jsonString)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Set up Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chartCard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize CheckBoxes
        toggleTemperature = findViewById(R.id.toggleTemperature)
        toggleVibration = findViewById(R.id.toggleVibration)
        toggleHumidity = findViewById(R.id.toggleHumidity)
        toggleCurrent = findViewById(R.id.toggleCurrent)
        toggleVoltage = findViewById(R.id.toggleVoltage)

        barChart = findViewById(R.id.barChart)
        setupBarChart()

        // Set up CheckBox listeners
        toggleTemperature.setOnCheckedChangeListener { _, isChecked ->
            updateChartVisibility()
        }
        toggleVibration.setOnCheckedChangeListener { _, isChecked ->
            updateChartVisibility()
        }
        toggleHumidity.setOnCheckedChangeListener { _, isChecked ->
            updateChartVisibility()
        }
        toggleCurrent.setOnCheckedChangeListener { _, isChecked ->
            updateChartVisibility()
        }
        toggleVoltage.setOnCheckedChangeListener { _, isChecked ->
            updateChartVisibility()
        }

        // Set up FAB to clear chart
        val fabRefresh: FloatingActionButton = findViewById(R.id.fabRefresh)
        fabRefresh.setOnClickListener {
            temperatureEntries.clear()
            vibrationEntries.clear()
            humidityEntries.clear()
            currentEntries.clear()
            voltageEntries.clear()
            timestamps.clear()
            entryIndex = 0f
            setupBarChart()
        }

        // Register BroadcastReceiver for sensor data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.e("RegisterCalled", "Here")
            registerReceiver(sensorDataReceiver, IntentFilter("com.example.mqttalert.SENSOR_DATA"), RECEIVER_EXPORTED)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorDataReceiver)
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true
        barChart.isDragYEnabled = true // Enable vertical dragging
        barChart.setScaleEnabled(true)
        barChart.setScaleYEnabled(true) // Enable vertical scaling
        barChart.setPinchZoom(true)
        barChart.setBackgroundColor(android.graphics.Color.WHITE)
        barChart.animateY(1000) // Animate bars vertically on setup

        // Set horizontal scrolling limits
        barChart.setVisibleXRangeMaximum(visibleXRange)
        barChart.moveViewToX(entryIndex - visibleXRange) // Scroll to latest data

        // Add tap listener for bar selection
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val label = barChart.data.getDataSetByIndex(h.dataSetIndex).label
                Toast.makeText(this@DashboardActivity, "$label: ${e.y}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {}
        })

        // Customize X-axis
        barChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < timestamps.size) {
                        dateFormat.format(Date(timestamps[index]))
                    } else {
                        ""
                    }
                }
            }
            granularity = 1f
            setDrawGridLines(false)
            textColor = android.graphics.Color.BLACK
            textSize = 10f // Smaller text for date-time
            setCenterAxisLabels(true) // Center labels under grouped bars
            labelRotationAngle = 45f // Rotate labels for readability
        }

        // Customize Y-axis
        barChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
            textColor = android.graphics.Color.BLACK
            textSize = 12f
            gridColor = android.graphics.Color.LTGRAY
        }
        barChart.axisRight.isEnabled = false

        // Customize Legend
        barChart.legend.apply {
            isEnabled = true
            form = Legend.LegendForm.SQUARE
            textSize = 14f
            textColor = android.graphics.Color.BLACK
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }

//        // Initialize datasets
//        val temperatureDataSet = BarDataSet(temperatureEntries, "Temperature (°C)").apply {
//            color = android.graphics.Color.parseColor("#FF5722") // Deep Orange
//            valueTextColor = android.graphics.Color.BLACK
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return String.format("%.1f", value)
//                }
//            }
//        }
//
//        val vibrationDataSet = BarDataSet(vibrationEntries, "Vibration (Hz)").apply {
//            color = android.graphics.Color.parseColor("#2196F3") // Blue
//            valueTextColor = android.graphics.Color.BLACK
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return String.format("%.1f", value)
//                }
//            }
//        }
//
//        val humidityDataSet = BarDataSet(humidityEntries, "Humidity (%)").apply {
//            color = android.graphics.Color.parseColor("#4CAF50") // Green
//            valueTextColor = android.graphics.Color.BLACK
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return String.format("%.1f", value)
//                }
//            }
//        }
//
//        val currentDataSet = BarDataSet(currentEntries, "Current (A)").apply {
//            color = android.graphics.Color.parseColor("#FFC107") // Amber
//            valueTextColor = android.graphics.Color.BLACK
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return String.format("%.2f", value)
//                }
//            }
//        }
//
//        val voltageDataSet = BarDataSet(voltageEntries, "Voltage (V)").apply {
//            color = android.graphics.Color.parseColor("#9C27B0") // Purple
//            valueTextColor = android.graphics.Color.BLACK
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return String.format("%.2f", value)
//                }
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.e("RegisterCalled", "Here")
            registerReceiver(sensorDataReceiver, IntentFilter("com.example.mqttalert.SENSOR_DATA"), RECEIVER_EXPORTED)
        }

//        // Apply visibility based on CheckBox state
//        temperatureDataSet.isVisible = toggleTemperature.isChecked
//        vibrationDataSet.isVisible = toggleVibration.isChecked
//        humidityDataSet.isVisible = toggleHumidity.isChecked
//        currentDataSet.isVisible = toggleCurrent.isChecked
//        voltageDataSet.isVisible = toggleVoltage.isChecked
//
//        val barData = BarData(temperatureDataSet, vibrationDataSet, humidityDataSet, currentDataSet, voltageDataSet).apply {
//            barWidth = 0.18f // Narrower bars for 5 datasets
//        }
//        barChart.data = barData
//        barChart.groupBars(0f, 0.05f, 0.01f) // Group 5 bars with small spacing
        barChart.invalidate()
    }

    private fun updateChartVisibility() {
        val barData = barChart.data
        if (barData != null) {
            barData.getDataSetByIndex(0)?.isVisible = toggleTemperature.isChecked // Temperature
            barData.getDataSetByIndex(1)?.isVisible = toggleVibration.isChecked // Vibration
            barData.getDataSetByIndex(2)?.isVisible = toggleHumidity.isChecked // Humidity
            barData.getDataSetByIndex(3)?.isVisible = toggleCurrent.isChecked // Current
            barData.getDataSetByIndex(4)?.isVisible = toggleVoltage.isChecked // Voltage
            barChart.notifyDataSetChanged()
            barChart.invalidate()
        }
    }

    private fun processSensorData(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            if (!json.has("temperature") || !json.has("vibration") || !json.has("humidity") ||
                !json.has("current") || !json.has("voltage")) {
                Log.e("Dashboard", "Missing required JSON fields")
                return
            }

            val temperatureStr = json.getString("temperature").replace("[^\\d.-]".toRegex(), "")
            val temperature = temperatureStr.toDoubleOrNull() ?: 0.0
            val vibration = json.optDouble("vibration", 0.0)
            val humidityStr = json.getString("humidity").replace("[^\\d.-]".toRegex(), "")
            val humidity = humidityStr.toDoubleOrNull() ?: 0.0
            val currentStr = json.getString("current").replace("[^\\d.-]".toRegex(), "")
            val current = currentStr.toDoubleOrNull() ?: 0.0
            val voltageStr = json.getString("voltage").replace("[^\\d.-]".toRegex(), "")
            val voltage = voltageStr.toDoubleOrNull() ?: 0.0

            // Store timestamp and add entries
            val timestamp = System.currentTimeMillis()
            timestamps.add(timestamp)
            temperatureEntries.add(BarEntry(entryIndex, temperature.toFloat()))
            vibrationEntries.add(BarEntry(entryIndex, vibration.toFloat()))
            humidityEntries.add(BarEntry(entryIndex, humidity.toFloat()))
            currentEntries.add(BarEntry(entryIndex, current.toFloat()))
            voltageEntries.add(BarEntry(entryIndex, voltage.toFloat()))
            entryIndex += 1f

            // Update chart
            val temperatureDataSet = BarDataSet(temperatureEntries, "Temperature (°C)").apply {
                color = android.graphics.Color.parseColor("#FF5722")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f", value)
                    }
                }
            }

            val vibrationDataSet = BarDataSet(vibrationEntries, "Vibration (Hz)").apply {
                color = android.graphics.Color.parseColor("#2196F3")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f", value)
                    }
                }
            }

            val humidityDataSet = BarDataSet(humidityEntries, "Humidity (%)").apply {
                color = android.graphics.Color.parseColor("#4CAF50")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f", value)
                    }
                }
            }

            val currentDataSet = BarDataSet(currentEntries, "Current (A)").apply {
                color = android.graphics.Color.parseColor("#FFC107")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            val voltageDataSet = BarDataSet(voltageEntries, "Voltage (V)").apply {
                color = android.graphics.Color.parseColor("#9C27B0")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            // Apply visibility based on CheckBox state
            temperatureDataSet.isVisible = toggleTemperature.isChecked
            vibrationDataSet.isVisible = toggleVibration.isChecked
            humidityDataSet.isVisible = toggleHumidity.isChecked
            currentDataSet.isVisible = toggleCurrent.isChecked
            voltageDataSet.isVisible = toggleVoltage.isChecked

            val barData = BarData(temperatureDataSet, vibrationDataSet, humidityDataSet, currentDataSet, voltageDataSet).apply {
                barWidth = 0.18f
            }
            barChart.data = barData
            barChart.groupBars(0f, 0.05f, 0.01f)
            barChart.notifyDataSetChanged()
            barChart.animateY(500) // Animate updates
            barChart.moveViewToX(entryIndex - visibleXRange) // Scroll to latest data
            barChart.invalidate()
        } catch (e: Exception) {
            Log.e("Dashboard", "JSON parsing error: ${e.message}")
        }
    }

}
