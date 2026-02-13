#include <Arduino.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "secret.h"

// Task Handlers (optional, but good practice)
TaskHandle_t networkConnectionTask;
TaskHandle_t sensorReadingTask;
QueueHandle_t readingQueue;

WiFiClientSecure wifiClient;
int status = WL_IDLE_STATUS;

PubSubClient pubSubClient(wifiClient);

// --- Task 1: Runs on Core 0 ---
void networkConnection(void *pvParameters)
{
  for (;;) // Keep the task alive forever
  {
    // 1. ENSURE WIFI IS CONNECTED
    if (WiFi.status() != WL_CONNECTED)
    {
      WiFi.begin(ssid, pass);
      while (WiFi.status() != WL_CONNECTED)
      {
        vTaskDelay(pdMS_TO_TICKS(500));
        Serial.print(".");
      }
      Serial.println("\nConnected to WiFi");
    }

    // 2. ENSURE MQTT IS CONNECTED
    if (!pubSubClient.connected())
    {
      Serial.println("Attempting MQTT connection...");
      // Try to connect
      // Update the connect line to include credentials
      if (pubSubClient.connect("ESP32_Plant_Project", mqtt_user, mqtt_pass))
      {
        Serial.println("Connected to HiveMQ Cloud!");
      }
      else
      {
        Serial.print("MQTT Failed, state code: ");
        Serial.println(pubSubClient.state());
        vTaskDelay(pdMS_TO_TICKS(5000)); // Wait 5s before retrying
        continue;
      }
    }

    // 3. THE HEARTBEAT (Crucial)
    pubSubClient.loop();

    // 4. PERIODIC PUBLISH
    // Using a simple timer so we don't spam the broker every millisecond

    int receivedVal;
    if (xQueueReceive(readingQueue, &receivedVal, pdMS_TO_TICKS(100)) == pdPASS)
    {
      JsonDocument plantDataToSend;
      plantDataToSend["reading"] = receivedVal;

      char payload[32];

      serializeJson(plantDataToSend, payload);

      pubSubClient.publish("plantData", payload);
    }

    vTaskDelay(pdMS_TO_TICKS(10)); // Tiny yield to keep Watchdog happy
  }
}

// --- Task 2: Runs on Core 1 ---
void readSensorData(void *pvParameters)
{
  Serial.print("Sensor reading task running on core ");
  Serial.println(xPortGetCoreID());
  for (;;)
  {
    int dummyData = random(0, 100);
    xQueueSend(readingQueue, &dummyData, 0);
    Serial.printf("Generated Dummy Data: %.2f\n", dummyData);
    vTaskDelay(pdMS_TO_TICKS(5000));
  }
}

void setup()
{
  Serial.begin(115200);
  wifiClient.setInsecure();
  pubSubClient.setServer(mqtt_server, 8883);

  // Create a queue that can hold 20 integers
  readingQueue = xQueueCreate(20, sizeof(int));

  if (readingQueue == NULL)
  {
    Serial.println("Queue creation failed!");
  }

  // Create Task 1: "Task1", Stack size 10000, Priority 1, pinned to Core 0
  xTaskCreatePinnedToCore(
      networkConnection,       /* Task function. */
      "NetworkConnectionTask", /* name of task. */
      10000,                   /* Stack size of task */
      NULL,                    /* parameter of the task */
      1,                       /* priority of the task */
      &networkConnectionTask,  /* Task handle to keep track of created task */
      0);                      /* pin task to core 0 */
  delay(500);

  // Create Task 2: pinned to Core 1
  xTaskCreatePinnedToCore(
      readSensorData,
      "SensorReadingTask",
      10000,
      NULL,
      1,
      &sensorReadingTask,
      1); /* pin task to core 1 */
  delay(500);
}

void loop()
{
  // In FreeRTOS on ESP32, the loop() is actually its own low-priority task.
  // We can leave it empty or use it for system heartbeats.
  vTaskDelete(NULL); // Or just delete this task if not needed
}