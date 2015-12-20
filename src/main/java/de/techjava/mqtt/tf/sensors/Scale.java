package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tinkerforge.BrickletLoadCell;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.scale.uid")
public class Scale {

	private Logger logger = LoggerFactory.getLogger(Scale.class);
	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@Autowired
	private TinkerForgeInitializerAspect initializer;
	@TinkerForgeUid
	private String uid;
	@Value("#{tinkerforge.bricklet.scale.callbackperiod ?: 1000}")
	private long callbackperiod;

	private BrickletLoadCell load;

	@PostConstruct
	public void init() {
		initializer.initalizeComponent(this);
		load = new BrickletLoadCell(uid, ipcon);

		load.addWeightListener((weight) -> {
			sender.sendMessage("weight", String.valueOf(weight));
		});
		logger.info("Scale initilized");
		try {
			load.setWeightCallbackPeriod(callbackperiod);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
	}
}
