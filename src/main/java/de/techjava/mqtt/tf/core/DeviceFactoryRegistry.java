package de.techjava.mqtt.tf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for asset factories.
 * @author Simon Zambrovski
 */
public class DeviceFactoryRegistry {

    private Map<Integer, List<DeviceFactory>> factories = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DeviceFactoryRegistry.class);

    public void registerDeviceFactory(final int type, final DeviceFactory factory) {
		logger.info("Registering new factory {} for device type {}", factory.getClass().getSimpleName(), type);
		List<DeviceFactory> registeredFactories = factories.get(Integer.valueOf(type));
		if (registeredFactories == null)  {
		    registeredFactories = new ArrayList<>();
		    factories.put(Integer.valueOf(type), registeredFactories);
		}
		registeredFactories.add(factory);
	}

    public List<DeviceFactory> getDeviceFactory(int deviceIdentifier) {
        return factories.get(Integer.valueOf(deviceIdentifier));
    }

}
