package de.techjava.mqtt.tf.listeners;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.techjava.mqtt.tf.comm.MqttListener;

/**
 * @author Thorsten Pohl, U412681
 */
@Component
public class TemperatureChangedListener implements MqttCallback {

    private Logger logger = LoggerFactory.getLogger(TemperatureChangedListener.class);
    @Autowired
    private MqttListener listener;

    /**
     * 
     */
    public TemperatureChangedListener() {

    }

    @PostConstruct
    public void init() {
        logger.info("INIT LISTENER");
        try {
            listener.registerListener("temperature", this);
        } catch (MqttException e) {
            logger.error("Cannot register Listener", e);
        }
    }

    @Override
    public void connectionLost(Throwable arg0) {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info(topic);
        logger.info("Temperature {}", message.toString());

    }
}
