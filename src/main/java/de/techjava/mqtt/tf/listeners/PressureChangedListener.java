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
//@Component
public class PressureChangedListener extends MqttCallbackAdapter {

	private Logger logger = LoggerFactory.getLogger(PressureChangedListener.class);
	@Autowired
	private MqttReceiver listener;

	@Autowired
	private MqttSender sender;

	@PostConstruct
	public void init() {
		listener.addListener("pressure", this);
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws Exception {
		// sender.sendMessage("lcd/text2", message.toString());
		logger.info("Pressure on {} is {}", topic, message.toString());
	}
}
