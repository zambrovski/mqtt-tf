package de.techjava.mqtt.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Starter {

	static final Logger logger = LoggerFactory.getLogger(Starter.class);

	public static void main(final String[] args) {
		final ConfigurableApplicationContext applicationContext = SpringApplication.run(Starter.class, args);
		applicationContext.addApplicationListener((event) -> {
			
		});
	}

}
