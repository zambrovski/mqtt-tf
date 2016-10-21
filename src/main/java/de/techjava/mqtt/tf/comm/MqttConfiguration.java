package de.techjava.mqtt.tf.comm;

import java.util.UUID;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class MqttConfiguration {

    private Logger logger = LoggerFactory.getLogger(MqttConfiguration.class);
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttClient getMqttClient() {
        try {
            if (clientId == null) {
                clientId = UUID.randomUUID().toString();
                logger.info("Using a client {}", clientId);
            }
            client = new MqttClient(broker, clientId, persistence);
            final MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            logger.info("Connecting to broker: {}...", broker);
            client.connect(connOpts);
            logger.info("Connected.");
        } catch (MqttException e) {
            logger.error("Error establishing MQTT connection", e);
        }
        return client;
    }

    /**
     * Disconnect on destroy.
     */
    @PreDestroy
    public void destroy() {
        try {
            if (client != null) {
                client.disconnect();
                logger.info("Disconnected from broker: " + broker);
            }
        } catch (MqttException e) {
            logger.error("Error disconnecting", e);
        }
    }
}
