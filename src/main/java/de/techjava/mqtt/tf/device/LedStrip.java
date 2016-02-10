package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.IPConnection;

import de.techjava.mqtt.tf.comm.MqttCallbackAdapter;
import de.techjava.mqtt.tf.comm.MqttReceiver;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class LedStrip implements DeviceFactory {

    private static final Logger logger = LoggerFactory.getLogger(LedStrip.class);

    private static final int NUM_LEDS = 16;
    private static int rIndex = 0;
    private static short[] r = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static short[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static short[] b = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Value("${tinkerforge.led.topic?:led}")
    private String topic;

    // frame duration to 50ms (20 frames per second)
    @Value("${tinkerforge.led.frameduration?:50}")
    private Integer frameduration;

    
    @Autowired
    private IPConnection ipcon;
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
    public void createDevice(final String uid) {
        final BrickletLEDStrip ls = new BrickletLEDStrip(uid, ipcon);
        final MqttCallback callback = new MqttCallbackAdapter() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                ls.setFrameDuration(frameduration);

                // Use frame rendered callback to move the active LED every frame
                ls.addFrameRenderedListener(new BrickletLEDStrip.FrameRenderedListener() {
                    public void frameRendered(int length) {
                        b[rIndex] = 0;
                        if (rIndex == NUM_LEDS - 1) {
                            rIndex = 0;
                        } else {
                            rIndex++;
                        }
                        b[rIndex] = 255;

                        // Set new data for next render cycle
                        try {
                            ls.setRGBValues(0, (short) NUM_LEDS, r, g, b);
                        } catch (Exception e) {
                            logger.error("Error setting LED", e);
                        }
                    }
                });

                // Set initial rgb values to get started
                ls.setRGBValues(0, (short) NUM_LEDS, r, g, b);
                logger.info("Led Strip updated.");
            }

        };
        receiver.addListener(realm.getTopic(uid) + topic, callback);

        logger.info("Led Strip sensor with uid {} initialized.", uid);
    }
}
