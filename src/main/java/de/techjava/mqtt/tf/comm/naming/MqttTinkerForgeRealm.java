package de.techjava.mqtt.tf.comm.naming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "file:realm.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:realm.properties", ignoreResourceNotFound = true)
public class MqttTinkerForgeRealm {

	private static final String SEP = "/";
	@Autowired
	private Environment env;

	public String getTopic(final String uid) {
		return env.getProperty(uid + ".topic", uid) + SEP;
	}

	public long getCallback(String uid, long callbackperiod) {
		final String prop = env.getProperty(uid);
		if (prop != null) {
			try {
				int value = Integer.parseInt(prop);
				return value;
			} catch (NumberFormatException e) {

			}
		}
		return callbackperiod;
	}
}
