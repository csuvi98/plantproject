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
    MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "plantMonitorClient", mqttClientFactory(), "plantData");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return message -> {
            try {
                String payload = message.getPayload().toString();
                System.out.println("New MQTT Message: " + payload);

                ObjectMapper mapper = new ObjectMapper();
                PlantData newPlantData = mapper.readValue(payload, PlantData.class);

                newPlantData.setDate(LocalDateTime.now());
                newPlantData = plantDataService.save(newPlantData);

                System.out.println("Successfully saved reading: " + newPlantData.getReading() + " "
                        + newPlantData.getDate() + " ID " + newPlantData.getId());
            } catch (Exception e) {
                System.err.println("Failed to process MQTT message: " + e.getMessage());
            }
        };
    }
}
