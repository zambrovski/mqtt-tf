package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.barometer.uid")
public class Barometer {

	private Logger logger = LoggerFactory.getLogger(Barometer.class);
	@Autowired
	private IPConnection ipcon;
	@Autowired
	private TinkerForgeInitializerAspect initializer;
	@Autowired
	private MqttSender sender;

	@TinkerForgeUid
	private String uid;

	@Value("${tinkerforge.bricklet.barometer.callbackperiod ?: 1000}")
	private long callbackperiod;

	private BrickletBarometer barometer;

	@PostConstruct
	public void init() {
		initializer.initalizeComponent(this);
		barometer = new BrickletBarometer(uid, ipcon);
		barometer.addAirPressureListener((airPressure) -> {
			sender.sendMessage("pressure", String.valueOf(airPressure));
		});
		logger.info("Barometer initilized");
		try {
			barometer.setAirPressureCallbackPeriod(callbackperiod);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
	}
}
