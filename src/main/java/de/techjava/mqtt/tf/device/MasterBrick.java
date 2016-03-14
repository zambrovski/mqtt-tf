package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickMaster;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class MasterBrick implements DeviceFactory<BrickMaster>, DeviceController<BrickMaster> {

    private static final Logger logger = LoggerFactory.getLogger(MasterBrick.class);
    @Autowired
    private IPConnection ipcon;
    @Autowired
    private DeviceFactoryRegistry registry;

    @PostConstruct
    public void init() {
        registry.registerDeviceFactory(BrickMaster.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickMaster.DEVICE_IDENTIFIER, this);
    }

    @Override
    public BrickMaster createDevice(String uid) {
        BrickMaster master = new BrickMaster(uid, ipcon);
        return master;
    }

    @Override
    public void setupDevice(final String uid, final BrickMaster master) {
        try {
            master.enableStatusLED();
            logger.info("Master brick with uid {} initialized.", uid);
        } catch (
                 TimeoutException | NotConnectedException e) {
            logger.error("Error accessing master brick", e);
        }
    }
}
