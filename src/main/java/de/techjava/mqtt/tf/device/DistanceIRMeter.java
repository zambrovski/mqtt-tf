package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class DistanceIRMeter implements DeviceFactory<BrickletDistanceIR>, DeviceController<BrickletDistanceIR> {

    private Logger logger = LoggerFactory.getLogger(DistanceIRMeter.class);
    @Value("${tinkerforge.distance.ir.callbackperiod?:500}")
    private long callbackperiod;
    @Value("${tinkerforge.distance.ir.topic?:distance}")
    private String topic;

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
        registry.registerDeviceFactory(BrickletDistanceIR.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletDistanceIR.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletDistanceIR createDevice(String uid) {
        BrickletDistanceIR bricklet = new BrickletDistanceIR(uid, ipcon);
        return bricklet;
    }

    @Override
    public void setupDevice(final String uid, final BrickletDistanceIR sensor) {

        boolean enable = !envHelper.isDisabled(uid, DistanceIRMeter.class);

        if (enable) {
            sensor.addDistanceListener((distance) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, distance);
            });
        } else {
            logger.info("IR distance listener disabled");
        }
        try {
            if (enable) {
                sensor.setDistanceCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
            }
        } catch (TimeoutException | NotConnectedException e) {
            logger.error("Error setting callback period", e);
        }
        logger.info("IR distance with uid {} initialized", uid);
    }
}
