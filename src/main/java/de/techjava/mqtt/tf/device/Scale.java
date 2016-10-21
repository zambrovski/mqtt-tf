package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletLoadCell;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class Scale implements DeviceFactory<BrickletLoadCell>, DeviceController<BrickletLoadCell> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scale.class);
    
    @Value("${tinkerforge.scale.callbackperiod?: 5000}")
    private long callbackperiod;
    @Value("${tinkerforge.scale.topic?:weight}")
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
        registry.registerDeviceFactory(BrickletLoadCell.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletLoadCell.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletLoadCell createDevice(String uid) {
        return new BrickletLoadCell(uid, ipcon);
    }

    @Override
    public void setupDevice(final String uid, final BrickletLoadCell sensor) {
        boolean enable = !envHelper.isDisabled(uid, Scale.class);
        if (enable) {
            sensor.addWeightListener((weight) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, weight);
            });
            try {
                sensor.setWeightCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
            } catch (TimeoutException | NotConnectedException e) {
                LOGGER.error("Error setting callback period", e);
            }
        } else {
            LOGGER.info("{} listener disabled.", getClass().getSimpleName());
        }
        LOGGER.info("{} with uid {} initilized.", getClass().getSimpleName(), uid);
    }
}
