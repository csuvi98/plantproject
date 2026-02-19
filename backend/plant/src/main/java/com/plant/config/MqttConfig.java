package com.plant.config;

import java.time.LocalDateTime;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.plant.dao.PlantData;
import com.plant.service.PlantDataService;

import tools.jackson.databind.ObjectMapper;

/*  This class is responsible for catching MQTT messages
    sent from the sensor to the HiveMQ broker.
    It also converts the readings to PlantData and saves
    them in the DB with PlantDataService. */
@Configuration
public class MqttConfig {

    @Value("${app.mqtt.url}")
    private String mqttAddr;

    @Value("${app.mqtt.user}")
    private String mqttUser;

    @Value("${app.mqtt.pw}")
    private String mqttPw;

    // Direct dependency injection for the service
    private final PlantDataService plantDataService;

    public MqttConfig(PlantDataService plantDataService) {
        this.plantDataService = plantDataService;
    }

    // Set up MQTT client, with username and password configured on HiveMQ
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { mqttAddr });
        options.setUserName(mqttUser);
        options.setPassword(mqttPw.toCharArray());
        options.setAutomaticReconnect(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    ObjectMapper mqttObjectMapper() {
        return new ObjectMapper();
    }

    // Inbound data is monitored on the plantData topic, sets up MQTT client
    // specified above
    @Bean
    MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "plantMonitorClient", mqttClientFactory(), "plantData");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /*
     * Upon receiving data, we log the details, convert it to
     * PlantData, and save it in the DB with PlantDataService
     */

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler(ObjectMapper mqttObjectMapper) {
        return message -> {
            try {
                String payload = message.getPayload().toString();
                System.out.println("New MQTT Message: " + payload);

                // ObjectMapper maps the "reading" JSON data to "reading" PlantData variable
                PlantData newPlantData = mqttObjectMapper.readValue(payload, PlantData.class);

                newPlantData.setDate(LocalDateTime.now());
                newPlantData = plantDataService.save(newPlantData);

                System.out.println("Successfully saved reading: " + newPlantData.getReading() + " "
                        + newPlantData.getPercentage() + " "
                        + newPlantData.getDate() + " ID " + newPlantData.getId());
            } catch (Exception e) {
                System.err.println("Failed to process MQTT message: " + e.getMessage());
            }
        };
    }
}
