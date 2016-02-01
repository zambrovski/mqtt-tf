package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class DistanceIRMeter implements DeviceFactory {

	private Logger logger = LoggerFactory.getLogger(DistanceIRMeter.class);
	@Value("${tinkerforge.distance.ir.callbackperiod?:500}")
	private long callbackperiod;
	@Value("${tinkerforge.distance.ir.topic?:distance}")
	private String topic;

	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@Autowired
	private DeviceFactoryRegistry registry;
	@Autowired
	private EnvironmentHelper realm;

	@PostConstruct
	public void init() {
		registry.registerDeviceFactory(BrickletDistanceIR.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletDistanceIR sensor = new BrickletDistanceIR(uid, ipcon);
		sensor.addDistanceListener((distance) -> {
			sender.sendMessage(realm.getTopic(uid) + topic, distance);
		});
		try {
			sensor.setDistanceCallbackPeriod(realm.getCallback(uid, callbackperiod));
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
		logger.info("IR distance with uid {} initialized", uid);
	}
}
