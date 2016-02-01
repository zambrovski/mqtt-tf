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

	public Threshold getThreshold(String uid, String typeProperty) {
		final String prop = env.getProperty("uid" + ".threshold");
		if (prop != null) {
			return Threshold.parse(prop);
		} else {
			return Threshold.parse(typeProperty);
		}
	}
}
