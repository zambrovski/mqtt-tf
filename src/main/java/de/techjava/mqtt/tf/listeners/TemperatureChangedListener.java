package de.techjava.mqtt.tf.listeners;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.techjava.mqtt.tf.comm.MqttCallbackAdapter;
import de.techjava.mqtt.tf.comm.MqttReceiver;
import de.techjava.mqtt.tf.comm.MqttSender;

/**
 * @author Thorsten Pohl
 */
// @Component
public class TemperatureChangedListener extends MqttCallbackAdapter {

	private Logger logger = LoggerFactory.getLogger(TemperatureChangedListener.class);
	@Autowired
	private MqttReceiver listener;
	@Autowired
	private MqttSender sender;

	@PostConstruct
	public void init() {
		listener.addListener("temperature", this);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// sender.sendMessage("lcd/text1", message.toString());
		logger.info("Temperature on {} is {}", topic, message.toString());
	}
}
