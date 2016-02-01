package de.techjava.mqtt.tf.comm;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MqttSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSender.class);

    @Autowired
    private MqttClient client;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.topic.prefix}")
    private String topicPrefix;

    public void sendMessage(final String topic, final Object content) {

        final String message = seralizeMessage(topic, content);
        if (message != null) {
            sendMessage(topic, message);
        }
    }

    public static String seralizeMessage(final String topic, final Object content) {
        final Message message = new Message(content);
        final ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing message on topic {} eith content {}", topic, content, e);
        }
        return null;
    }

    private void sendMessage(final String topic, final String content) {
        LOGGER.trace("Publishing message to {}: {}", topic, content);

        final MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        try {
            client.publish(topicPrefix + topic, message);
        } catch (MqttException e) {
            LOGGER.error("Error sending message", e);
        }

        LOGGER.trace("Message published");
    }
}
