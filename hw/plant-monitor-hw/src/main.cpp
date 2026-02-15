#include <Arduino.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "secret.h"

#define uS_TO_S_FACTOR 1000000 // uS to S conversion
#define TIME_TO_SLEEP 10       // time to sleep in seconds

const int sensorPin = 34; // GPIO pin for sensor

WiFiClientSecure wifiClient;           // WiFi client
PubSubClient pubSubClient(wifiClient); // MQTT client

/*
    The ESP32 wakes up from deep sleep, measures the soil moisture sensor readings,
    connects to the WiFi, then the HiveMQ broker, and then publishes the reading
    to the plantData topic, that the backend will consume. Then, it goes back to
    deep sleep to conserve energy.
*/
void setup()
{
    // For debugging purposes
    Serial.begin(115200);

    // Configure WiFi and MQTT clients + deep sleep
    wifiClient.setInsecure();
    pubSubClient.setServer(mqtt_server, 8883);
    esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR);

    // Collecting soil moisture data
    Serial.println("Reading sensor data...");

    // Set resolution between 0 and 4096, default attenuation
    analogReadResolution(12);
    analogSetPinAttenuation(sensorPin, ADC_11db);

    int sensorData = analogRead(sensorPin);
    Serial.print("Sensor data: ");
    Serial.println(sensorData);

    // Connect to WiFi
    if (WiFi.status() != WL_CONNECTED)
    {
        WiFi.begin(ssid, pass);
        while (WiFi.status() != WL_CONNECTED)
        {
            delay(500);
            Serial.print(".");
        }
        Serial.println("\nConnected to WiFi");
    }

    // Connect to MQTT broker
    if (!pubSubClient.connected())
    {
        Serial.println("Attempting MQTT connection...");

        if (pubSubClient.connect("ESP32_Plant_Project", mqtt_user, mqtt_pass))
        {
            Serial.println("Connected to HiveMQ Cloud!");
        }
        else
        {
            Serial.print("MQTT Failed, state code: ");
            Serial.println(pubSubClient.state());
            delay(500);
        }
    }

    // Serialize data into JSON
    JsonDocument plantDataToSend;
    plantDataToSend["reading"] = sensorData;
    char payload[32];
    serializeJson(plantDataToSend, payload);

    // Publish data to MQTT
    pubSubClient.publish("plantData", payload);
    pubSubClient.loop();
    delay(200);
    Serial.println("Data published! Going back to sleep.");
    esp_deep_sleep_start();
}

void loop()
{
    // No need for loop due to deep sleep starting from setup()
}