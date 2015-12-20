package de.techjava.mqtt.tf.comm;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttSender {

	private Logger logger = LoggerFactory.getLogger(MqttSender.class);

	@Autowired
	private MqttClient client;

	@Value("${mqtt.qos}")
	private int qos;

	@Value("${mqtt.topic.prefix}")
	private String topicPrefix;

	public void sendMessage(final String topic, final String content) {
		logger.trace("Publishing message to {}: {}", topic, content);

		final MqttMessage message = new MqttMessage(content.getBytes());
		message.setQos(qos);
		try {
			client.publish(topicPrefix + topic, message);
		} catch (MqttException e) {
			logger.error("Error sending message", e);
		}

		logger.trace("Message published");
	}

}
