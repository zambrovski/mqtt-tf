package de.techjava.mqtt.tf.core;

import org.apache.commons.lang3.BooleanUtils;
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
     * 
     * @param classifier
     *            uid of device or uid.sensor
     * @param typeProperty
     *            type of
     * @return true if listener should be disabled, false otherwise
     */
    public <T extends DeviceFactory<?>> boolean isDisabled(final String classifier, final Class<T> deviceFactory) {
        // <device-uid>.disabled=true
        // <device-uid>.ambient.disabled=true
        final String instanceUidProperty = env.getProperty(classifier + ".disabled");
        
        if (BooleanUtils.toBoolean(instanceUidProperty)) {
            return true;
        }
        // tinkerforge.temperature.disabled=true
        final String factoryProperty = "tinkerforge." + deviceFactory.getSimpleName().toLowerCase() + ".disabled";
        
        return BooleanUtils.toBoolean(env.getProperty(factoryProperty));        
    }
    
    /**
     * Parses a long property.
     * 
     * @param uid
     *            uid to parse instance property
     * @param longProperty
     *            string injected into the device factory containing the long value.
     * @param suffix
     *            suffix to add to the uid
     * @return long property parsed from the instance or from type.
     */
    public long getLong(String uid, String longProperty, String suffix) {
        final String prop = env.getProperty(uid + "." + suffix);
        if (prop != null) {
            return Long.valueOf(prop);
        } else {
            return Long.valueOf(longProperty);
        }
    }

    /**
     * Parses a string property.
     * 
     * @param uid
     *            uid to parse instance property
     * @param longProperty
     *            string injected into the device factory containing the string value.
     * @param suffix
     *            suffix to add to the uid
     * @return string property parsed from the instance or from type.
     */
    public String getString(String uid, String stringProperty, String suffix) {
        final String prop = env.getProperty(uid + "." + suffix);
        if (prop != null) {
            return prop;
        } else {
            return stringProperty;
        }
    }

}
