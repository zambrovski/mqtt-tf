package de.techjava.mqtt.tf.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for asset factories.
 * 
 * @author Simon Zambrovski
 */
public class DeviceFactoryRegistry {

	private Map<Integer, DeviceFactory> factories = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(DeviceFactoryRegistry.class);

	public void registerDeviceFactory(final int type, final DeviceFactory factory) {
		logger.info("Registering new factory {} for device type {}", factory.getClass().getSimpleName(), type);
		factories.put(Integer.valueOf(type), factory);
	}

	public DeviceFactory getDeviceFactory(int deviceIdentifier) {
		return factories.get(Integer.valueOf(deviceIdentifier));
	}

}
