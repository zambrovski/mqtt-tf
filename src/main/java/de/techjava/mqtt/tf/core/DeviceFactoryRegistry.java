package de.techjava.mqtt.tf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerforge.Device;

/**
 * Registry for device factories.
 * 
 * @author Simon Zambrovski
 */
public class DeviceFactoryRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceFactoryRegistry.class);

    private Map<Integer, DeviceFactory<?>> factories = new HashMap<>();
    private Map<Integer, List<DeviceController<?>>> controllers = new HashMap<>();
    private Map<String, Device> devices = new HashMap<>();


    public void registerDeviceFactory(final int type, final DeviceFactory<?> factory) {
        LOG.info("Registering factory {} for device type {}", factory.getClass().getSimpleName(), type);
        factories.put(Integer.valueOf(type), factory);
    }

    public void registerDeviceController(final int type, final DeviceController<?> controller) {
        LOG.info("Registering controller {} for device type {}", controller.getClass().getSimpleName(), type);
        List<DeviceController<?>> registeredControllers = controllers.get(Integer.valueOf(type));
        if (registeredControllers == null) {
            registeredControllers = new ArrayList<>();
            controllers.put(Integer.valueOf(type), registeredControllers);
        }
        registeredControllers.add(controller);
    }

    public DeviceFactory<?> getDeviceFactory(final int deviceIdentifier) {
        return factories.get(Integer.valueOf(deviceIdentifier));
    }

    public List<DeviceController<?>> getDeviceControllers(final int deviceIdentifier) {
        return controllers.get(Integer.valueOf(deviceIdentifier));
    }

    public Device getDevice(final String uid) {
        return devices.get(uid);
    }

    public void createdDevice(final String uid, final Device device) {
        this.devices.put(uid, device);
    }
}
