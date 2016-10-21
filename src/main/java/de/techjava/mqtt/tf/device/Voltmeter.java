package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAnalogIn;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;
import de.techjava.mqtt.tf.core.Threshold;

@Component
public class Voltmeter implements DeviceFactory<BrickletAnalogIn>, DeviceController<BrickletAnalogIn> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Voltmeter.class);

    @Value("${tinkerforge.voltmeter.callbackperiod:100}")
    private long callbackperiod;
    @Value("${tinkerforge.voltmeter.topic:voltage}")
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
        registry.registerDeviceFactory(BrickletAnalogIn.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletAnalogIn.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletAnalogIn createDevice(String uid) {
        return new BrickletAnalogIn(uid, ipcon);
    }

    @Override
    public void setupDevice(final String uid, final BrickletAnalogIn sensor) {

        boolean enable = !envHelper.isDisabled(uid, Voltmeter.class);

        if (enable) {
            sensor.addVoltageListener((voltage) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, voltage);
            });
            sensor.addVoltageReachedListener((voltage) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, String.valueOf(voltage));
            });
            try {
                final Threshold threshold = envHelper.getThreshold(uid, "tinkerforge.voltmeter.threshold");
                if (threshold.isValid()) {
                    sensor.setVoltageCallbackThreshold(threshold.getOperation(), threshold.getMin(), threshold.getMax());
                } else {
                    sensor.setVoltageCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
                }

            } catch (TimeoutException | NotConnectedException e) {
                LOGGER.error("Error setting callback period", e);
            }
        } else {
            LOGGER.info("Voltmeter listener disabled");
        }
        LOGGER.info("Voltmeter with uid {} initialized.", uid);
    }
}
