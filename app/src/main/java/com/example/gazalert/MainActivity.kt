package com.example.gazalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazalert.ui.theme.GazAlertTheme
class MainActivity : ComponentActivity() {
    private lateinit var mqttManager: MqttManager
    private var gasLevel by mutableStateOf(0.0)
    private var publishTopic by mutableStateOf("security/gaz")
    private var publishQoS by mutableStateOf(0)
    private var publishMessage by mutableStateOf("11")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mqttManager = MqttManager(this)
        setContent {
            GazAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GasMonitorUI(gasLevel)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input fields for publishing
                        TextField(
                            value = publishTopic,
                            onValueChange = { publishTopic = it },
                            label = { Text("Topic") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = publishMessage,
                            onValueChange = { publishMessage = it },
                            label = { Text("Message") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Additional UI elements for QoS, Retain, etc., as needed

                        // Button to publish the message
                        Button(onClick = { publishMessage() }) {
                            Text("Publish Message")
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        // Disconnect and clean up the MQTT manager when the activity is destroyed
        mqttManager.disconnect()
        super.onDestroy()
    }
    private fun publishMessage() {
        mqttManager.publishMessage(publishTopic, publishMessage, publishQoS)
    }
    // Function to handle incoming gas levels and update the UI
    fun handleIncomingGasLevel(newGasLevel: Double) {
        // Use the value from MQTT to update the Compose UI
        gasLevel = newGasLevel
    }
}

@Composable
fun GasMonitorUI(gasLevel: Double) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Gas Level: $gasLevel")
        // Add more UI components as needed
    }
}
@Preview(showBackground = true)
@Composable
fun GasMonitorUIPreview() {
    GazAlertTheme {
        GasMonitorUI(0.0)
    }
}