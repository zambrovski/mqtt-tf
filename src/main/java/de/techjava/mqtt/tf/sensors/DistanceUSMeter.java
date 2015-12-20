package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.distance.us.uid")
public class DistanceUSMeter {

	private Logger logger = LoggerFactory.getLogger(DistanceUSMeter.class);
	@Autowired
	private TinkerForgeInitializerAspect initializer;
	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@TinkerForgeUid
	private String uid;
	@Value("${tinkerforge.bricklet.distance.us.callbackperiod ?: 1000}")
	private long callbackperiod;

	private BrickletDistanceUS distance;

	@PostConstruct
	public void init() {
		 initializer.initalizeComponent(this);
		distance = new BrickletDistanceUS(uid, ipcon);

		distance.addDistanceListener((distance) -> {
			sender.sendMessage("distance", String.valueOf(distance));
		});
		logger.info("Ultra-sound distance initilized");
		try {
			distance.setDistanceCallbackPeriod(callbackperiod);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
	}
}
