package de.techjava.mqtt.tf.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "file:realm.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:realm.properties", ignoreResourceNotFound = true)
public class EnvironmentHelper {

    private static final String SEP = "/";
    @Autowired
    private Environment env;

    /**
     * Retrieves a topic for device with given UID.
     * 
     * @param uid
     *            uid of device.
     * @return topic for given uid or the uid as fallback.
     */
    public String getTopic(final String uid) {
        return env.getProperty(uid + ".topic", uid) + SEP;
    }

    /**
     * Retrieves the value of the callback property for given uid.
     * 
     * @param uid
     *            uid of device.
     * @param callbackperiod
     *            default value.
     * @return configured callback period for uid or the callback.
     */
    public long getCallback(String uid, long callbackperiod) {
        final String prop = env.getProperty(uid + ".callback");
        if (prop != null) {
            try {
                int value = Integer.parseInt(prop);
                return value;
            } catch (NumberFormatException e) {

            }
        }
        return callbackperiod;
    }

    /**
     * Retrieves the value of the threshold for given uid.
     * 
     * @param uid
     *            uid of device.
     * @param typeProperty
     *            property of the measurement type
     * @return Threshold
     */
    public Threshold getThreshold(String uid, String typeProperty) {
        final String prop = env.getProperty(uid + ".threshold");
        if (prop != null) {
            return Threshold.parse(prop);
        } else {
            return Threshold.parse(typeProperty);
        }
    }

    /**
     * Retrieves the status of the measurement type listener for given type and uid.
     * @param uid uid of device
     * @param typeProperty type of
     * @return true if listener should be disabled, false otherwise
     */
    public boolean isDisabled(String uid, String typeProperty) {
        // <device-uid>.disabled=true
        // <device-uid>.ambient.disabled=true
        final String instanceUidProperty = env.getProperty(uid + ".disabled");
        if (instanceUidProperty != null && Boolean.parseBoolean(instanceUidProperty)) {
            return true;
        }
        // ${tinkerforge.temperature.disabled}=true
        // ${tinkerforge.temperature.ambient.disabled}=true
        if (typeProperty != null && Boolean.parseBoolean(typeProperty)) {
            return true;
        } 
        
        return false;
    }
}
