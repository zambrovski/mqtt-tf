package de.techjava.mqtt.tf.core;

import com.tinkerforge.Device;

/**
 * Implement your listener, actuators here. If you need a new device type
 * consider creating a {@link DeviceFactory} as well.
 * @author Gerold Schierholz
 * @param <T>
 *        the device to use.
 */
public interface DeviceController<T extends Device> {

    /**
     * Perform the setup of the Device.
     * @param uid
     *        the uid.
     * @param device
     *        the device created by a Factory.
     */
    void setupDevice(final String uid, final T device);
}
