package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;

@Component
public class Thermometer {

	private Logger logger = LoggerFactory.getLogger(Thermometer.class);
	@Autowired
	private IPConnection ipcon;

	@Autowired
	private MqttSender sender;

	@Value("${tinkerforge.bricklet.thermometer.uid}")
	private String uid;

	@Value("${tinkerforge.bricklet.thermometer.debounce}")
	private int debounce;

	private BrickletTemperature temperature;

	@PostConstruct
	public void init() {
		temperature = new BrickletTemperature(uid, ipcon);
		temperature.addTemperatureListener((temperature) -> {
			sender.sendMessage("temperature", String.valueOf(temperature));
		});
		logger.info("Thermometer initilized");
		try {
			temperature.setDebouncePeriod(debounce);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting debounce", e);
		}
	}
}
