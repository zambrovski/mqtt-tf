package de.techjava.mqtt.tf.comm;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic adapter to implement MQTT callbacks.
 * 
 * @author Simon Zambrovski
 */
public class MqttCallbackAdapter implements MqttCallback {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void connectionLost(Throwable cause) {
		logger.error("Connection lost", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}

}
