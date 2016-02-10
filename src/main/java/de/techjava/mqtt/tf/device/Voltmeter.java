package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAnalogIn;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;
import de.techjava.mqtt.tf.core.Threshold;

@Component
public class Voltmeter implements DeviceFactory {

    private Logger logger = LoggerFactory.getLogger(Voltmeter.class);
    @Value("${tinkerforge.voltmeter.callbackperiod?:100}")
    private long callbackperiod;
    @Value("${tinkerforge.voltmeter.topic?:voltage}")
    private String topic;
    @Value("${tinkerforge.voltmeter.disabled?:no}")
    private String disabled;

    @Autowired
    Environment env;
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
    }

    @Override
    public void createDevice(String uid) {

        BrickletAnalogIn sensor = new BrickletAnalogIn(uid, ipcon);

        boolean enable = !envHelper.isDisabled(uid, disabled);

        if (enable) {
            sensor.addVoltageListener((voltage) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, voltage);
            });
            sensor.addVoltageReachedListener((voltage) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, String.valueOf(voltage));
            });
        } else {
            logger.info("Voltmeter listener disabled");
        }

        try {
            if (enable) {
                Threshold threshold = envHelper.getThreshold(uid, "tinkerforge.voltmeter.threshold");
                if (threshold.isValid()) {
                    sensor.setVoltageCallbackThreshold(threshold.getOperation(), threshold.getMin(), threshold.getMax());
                } else {
                    sensor.setVoltageCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
                }
            }

        } catch (TimeoutException | NotConnectedException e) {
            logger.error("Error setting callback period", e);
        }
        logger.info("Voltmeter with uid {} initialized", uid);
    }
}
