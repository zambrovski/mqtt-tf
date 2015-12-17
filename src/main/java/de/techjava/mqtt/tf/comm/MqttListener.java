package de.techjava.mqtt.tf.comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
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
 * @author Thorsten Pohl
 */
@Component
public class MqttListener implements MqttCallback {

    private Map<String, Collection<MqttCallback>> listeners = new HashMap<>();

    public MqttListener() {
    }

    private Logger logger = LoggerFactory.getLogger(MqttListener.class);

    @Autowired
    private MqttClient client;

    @Value("${mqtt.qos}")
    int qos;

    @Value("${mqtt.topic.prefix}")
    String topicPrefix;

    @PostConstruct
    public void init() {
        logger.info("INIT LISTENER");
        client.setCallback(this);
    }

    public void registerListener(final String topic, final MqttCallback callback) throws MqttException {
        final String fullTopic = topicPrefix + topic;
        Collection<MqttCallback> callbacks = this.listeners.get(fullTopic);
        if (callbacks == null) {
            callbacks = new ArrayList<MqttCallback>();
            this.listeners.put(fullTopic, callbacks);
        }

        callbacks.add(callback);
        client.subscribe(listeners.keySet().toArray(new String[listeners.size()]));

    }

    @Override
    public void connectionLost(Throwable th) {
        // TODO Should this be forwarded?
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Should this be forwarded?
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
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
