package de.techjava.mqtt.tf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;

/**
 * Sends TF enumeration request.
 * 
 * @author Simon Zambrovski
 */
@Component
public class Enumerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Enumerator.class);

	@Autowired
	private IPConnection ipcon;

	@EventListener({ ContextStartedEvent.class, ContextRefreshedEvent.class })
	public void onApplicationEvent(final ApplicationContextEvent event) {
		try {
			ipcon.enumerate();
			LOGGER.info("Enumeration of connection started.");
		} catch (NotConnectedException e) {
			LOGGER.error("Error enumerating", e);
		}
	}

}
