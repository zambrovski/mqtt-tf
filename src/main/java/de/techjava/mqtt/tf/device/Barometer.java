package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class Barometer implements DeviceFactory<BrickletBarometer>, DeviceController<BrickletBarometer> {

    private Logger logger = LoggerFactory.getLogger(Barometer.class);
    @Value("${tinkerforge.barometer.topic?:pressure}")
    private String topic;
    @Value("${tinkerforge.barometer.callbackperiod?:10000}")
    private long callbackperiod;
    @Value("${tinkerforge.barometer.disabled?:no}")
    private String disabled;

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
        registry.registerDeviceFactory(BrickletBarometer.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletBarometer.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletBarometer createDevice(String uid) {
        BrickletBarometer bricklet = new BrickletBarometer(uid, ipcon);
        return bricklet;
    }

    @Override
    public void setupDevice(final String uid, final BrickletBarometer barometer) {
        boolean enable = !envHelper.isDisabled(uid, disabled);
        if (enable) {
            barometer.addAirPressureListener((airPressure) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, airPressure);
            });
        } else {
            logger.info("Barometer listener disabled");
        }
        try {
            if (enable) {
                barometer.setAirPressureCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
            }
        } catch (
                 TimeoutException | NotConnectedException e) {
            logger.error("Error setting callback period", e);
        }
        logger.info("Barometer with uid {} initilized.", uid);
    }
}
