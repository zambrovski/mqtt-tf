package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttCallbackAdapter;
import de.techjava.mqtt.tf.comm.MqttReceiver;
import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class LedStrip implements DeviceFactory {

    private static final Logger logger = LoggerFactory.getLogger(LedStrip.class);

    @Value("${tinkerforge.ambilight.topic?:led}")
    private String topic;

    @Value("${tinkerforge.ambilight.callbackperiod?:10000}")
    private long callbackperiod;

    @Autowired
    private IPConnection ipcon;
//    @Autowired
//    private MqttSender sender;
    @Autowired
    private DeviceFactoryRegistry registry;
    @Autowired
    private EnvironmentHelper realm;
    @Autowired
    private MqttReceiver receiver;

    @PostConstruct
    public void init() {
        registry.registerDeviceFactory(BrickletLEDStrip.DEVICE_IDENTIFIER, this);
    }

    @Override
    public void createDevice(String uid) {
        final BrickletLEDStrip bricklet = new BrickletLEDStrip(uid, ipcon);
        MqttCallback callback =  new MqttCallbackAdapter() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                short[] r = {1,128,255};
                short[] g = {1,128,255};
                short[] b = {1,128,255};
                
                bricklet.setRGBValues(0, (short) 3, r, g, b);
                logger.info("Led Strip updated.");
            }
            
        };
        receiver.addListener(realm.getTopic(uid) + topic, callback);
        
        logger.info("Led Strip sensor with uid {} initialized.", uid);
    }
}
