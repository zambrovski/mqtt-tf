package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.hygrometer.uid")
public class Hygrometer {

	private Logger logger = LoggerFactory.getLogger(Hygrometer.class);
	@Autowired
	private IPConnection ipcon;
//	@Autowired
//	private TinkerForgeInitializer initializer;
	@Autowired
	private MqttSender sender;
	@TinkerForgeUid
	private String uid;
	@Value("${tinkerforge.bricklet.hygrometer.callbackperiod ?: 1000}")
	private long callbackperiod;

	private BrickletHumidity hygrometer;

	@PostConstruct
	public void init() {
//		initializer.initalizeComponent(this);
		hygrometer = new BrickletHumidity(uid, ipcon);
		hygrometer.addHumidityListener((humidity) -> {
			sender.sendMessage("humidity", String.valueOf(humidity));
		});
		logger.info("Hygrometer initilized");
		try {
			hygrometer.setHumidityCallbackPeriod(callbackperiod);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
	}
}
