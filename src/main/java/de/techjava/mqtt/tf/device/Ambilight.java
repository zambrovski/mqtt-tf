package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class Ambilight implements DeviceFactory<BrickletAmbientLight>, DeviceController<BrickletAmbientLight> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ambilight.class);

    @Value("${tinkerforge.ambilight.topic?:illuminance}")
    private String topic;
    @Value("${tinkerforge.ambilight.callbackperiod?:10000}")
    private long callbackperiod;

    @Autowired
    private IPConnection ipcon;
    @Autowired
    private MqttSender sender;
    @Autowired
    private DeviceFactoryRegistry registry;
    @Autowired
    private EnvironmentHelper envHelper;

    @PostConstruct
    public void init() {
        registry.registerDeviceFactory(BrickletAmbientLight.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletAmbientLight.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletAmbientLight createDevice(final String uid) {
        return new BrickletAmbientLight(uid, ipcon);
    }

    @Override
    public void setupDevice(final String uid, final BrickletAmbientLight bricklet) {
        boolean enable = !envHelper.isDisabled(uid, Ambilight.class);
        if (enable) {
            bricklet.addIlluminanceListener((illuminance) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, illuminance);
            });
            try {
                bricklet.setIlluminanceCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
            } catch (TimeoutException | NotConnectedException e) {
                LOGGER.error("Error setting Illuminance Callback Period", e);
            }
        } else {
            LOGGER.info("{} listener disabled.", getClass().getSimpleName());
        }
        LOGGER.info("{} with uid {} initilized.", getClass().getSimpleName(), uid);
    }
}
