package de.techjava.mqtt.tf.core;

import com.tinkerforge.Device;

/**
 * Factory for creating devices.
 * @author Simon Zambrovski
 */
public interface DeviceFactory<T extends Device> {

    /**
     * Creates the device for specified uid.
     * @param uid
     *        uid of the device.
     */
    T createDevice(final String uid);

}
