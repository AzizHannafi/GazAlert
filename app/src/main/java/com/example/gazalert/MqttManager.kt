package com.example.gazalert

import android.content.Context
import android.util.Log
import com.example.gazalert.org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttManager(private val context: Context)  {

    private val serverUri = "tcp://broker.hivemq.com:1883"
    private val clientId = "AzizHannafiId"
    private val subscriptionTopic = "security/gaz"

    lateinit var mqttAndroidClient: MqttAndroidClient

    init {
        connect()
    }

    private fun connect() {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) {
                    // Reconnected to the server
                    println("Reconnected to : $serverURI")
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                } else {
                    // Connected to the server
                    println("Connected to: $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                // Connection lost
                println("The Connection was lost.")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // Message arrived on the subscribed topic
                val newGasLevel = message.toString()?.toDoubleOrNull()
                if (newGasLevel != null) {
                    println("Incoming message on $topic: $newGasLevel")
                    // Notify the MainActivity
                    (context as? MainActivity)?.handleIncomingGasLevel(newGasLevel)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Delivery complete
            }
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    // Connection successful
                    subscribeToTopic()

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    // Connection failed
                    println("Failed to connect to: $serverUri. Exception: $exception")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    // Subscription successful
                    println("Subscribed to $subscriptionTopic")
                    Log.d("MqttManager", "Subscribed to $subscriptionTopic")

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    // Subscription failed
                    println("Failed to subscribe to $subscriptionTopic")
                    Log.e("MqttManager", "Failed to subscribe to $subscriptionTopic")

                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publishMessage(topic: String, message: String, qos: Int = 0, retain: Boolean = false) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttMessage.qos = qos
            mqttMessage.isRetained = retain

            mqttAndroidClient.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttAndroidClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}