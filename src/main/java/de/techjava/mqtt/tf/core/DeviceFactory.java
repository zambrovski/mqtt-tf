package de.techjava.mqtt.tf.core;

/**
 * Factory for creating assets.
 * 
 * @author Simon Zambrovski
 *
 */
public interface DeviceFactory {

	/**
	 * Creates the device for specified uid.
	 * 
	 * @param uid
	 *            uid of the device.
	 */
	void createDevice(final String uid);
}
