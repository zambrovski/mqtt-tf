package de.techjava.mqtt.tf.comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MqttListener Bean. Manages all subscriptions to MQTT.
 * 
 * @author Thorsten Pohl
 */
@Component
public class MqttReceiver extends MqttCallbackAdapter {

	private Logger logger = LoggerFactory.getLogger(MqttReceiver.class);
	private Map<String, Collection<MqttCallback>> listeners = new HashMap<>();

	@Autowired
	private MqttClient client;

	@Value("${mqtt.qos}")
	private int qos;

	@Value("${mqtt.topic.prefix}")
	private String topicPrefix;

	@PostConstruct
	public void init() {
		client.setCallback(this);
		logger.info("MQTT receiver initialized.");
	}

	/**
	 * Registers a callback for receiving MQTT messages.
	 * 
	 * @param topic
	 *            topic to listen for messages.
	 * @param callback
	 *            listener to register.
	 */
	public void addListener(final String topic, final MqttCallback callback) {
		logger.info("Adding listener {} to topic '{}'", callback.getClass().getName(), topic);
		final String fullTopic = topicPrefix + topic;
		Collection<MqttCallback> callbacksForTopic = this.listeners.get(fullTopic);
		if (callbacksForTopic == null) {
			callbacksForTopic = new ArrayList<MqttCallback>();
			try {
				this.listeners.put(fullTopic, callbacksForTopic);
				this.client.subscribe(fullTopic);
				logger.info("Subscribed to topic '{}'", fullTopic);
			} catch (MqttException e) {
				logger.error("Error registering listener", e);
			}
		}
		callbacksForTopic.add(callback);
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws Exception {
		logger.info("Message arrived at '{}' with payload {}", topic, message);
		final Collection<MqttCallback> callbacks = this.listeners.get(topic);
		if (callbacks != null) {
			for (final MqttCallback mqttCallback : callbacks) {
				try {
					mqttCallback.messageArrived(topic, message);
				} catch (Exception e) {
					logger.warn("Exception in message processing", e);
				}
			}
		}
	}

}
