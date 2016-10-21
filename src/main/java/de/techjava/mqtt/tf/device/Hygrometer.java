package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class Hygrometer implements DeviceFactory<BrickletHumidity>, DeviceController<BrickletHumidity> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DistanceIRMeter.class);
    
    @Value("${tinkerforge.humidity.callbackperiod?:10000}")
    private long callbackperiod;
    @Value("${tinkerforge.humidity.topic?:humidity}")
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
        registry.registerDeviceFactory(BrickletHumidity.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletHumidity.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickletHumidity createDevice(String uid) {
        return new BrickletHumidity(uid, ipcon);
    }

    @Override
    public void setupDevice(final String uid, final BrickletHumidity sensor) {
        boolean enable = !envHelper.isDisabled(uid, Hygrometer.class);
        if (enable) {
            sensor.addHumidityListener((humidity) -> {
                sender.sendMessage(envHelper.getTopic(uid) + topic, humidity);
            });
            try {
                sensor.setHumidityCallbackPeriod(envHelper.getCallback(uid, callbackperiod));
            } catch (TimeoutException | NotConnectedException e) {
                LOGGER.error("Error setting callback period", e);
            }
        } else {
            LOGGER.info("{} listener disabled.", getClass().getSimpleName());
        }
        LOGGER.info("{} with uid {} initilized.", getClass().getSimpleName(), uid);
    }
}
