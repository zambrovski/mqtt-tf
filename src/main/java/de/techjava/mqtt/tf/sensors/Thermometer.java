package de.techjava.mqtt.tf.sensors;

import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.thermometer.uid")
public class Thermometer {

	private Logger logger = LoggerFactory.getLogger(Thermometer.class);
	@Autowired
	private IPConnection ipcon;
	@Autowired
	private TinkerForgeInitializerAspect initializer;
	@Autowired
	private MqttSender sender;

	@TinkerForgeUid
	private String uid;

	@Value("${tinkerforge.bricklet.thermometer.callbackperiod?: 10000}")
	private long callbackperiod;

	private BrickletTemperature temperature;

	
	
	@PostConstruct
	public void init() {
		initializer.initalizeComponent(this);

		Objects.requireNonNull(uid, "UID must not be null");
		Objects.requireNonNull(ipcon, "IP Connection must not be null");
		temperature = new BrickletTemperature(uid, ipcon);

		temperature.addTemperatureListener((temperature) -> {
			sender.sendMessage("temperature", String.valueOf(temperature));
		});
		logger.info("Thermometer initilized");
		try {
			temperature.setTemperatureCallbackPeriod(callbackperiod);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
	}
}
