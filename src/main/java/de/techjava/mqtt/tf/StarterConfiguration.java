package de.techjava.mqtt.tf;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import de.techjava.mqtt.tf.comm.MqttConfiguration;
import de.techjava.mqtt.tf.core.TinkerForgeConfiguration;

@Configuration
@Import({ MqttConfiguration.class, TinkerForgeConfiguration.class })
public class StarterConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(StarterConfiguration.class);

	@PostConstruct
	public void postConstruct() {
		logger.info("Configuration loaded.");
	}
}
