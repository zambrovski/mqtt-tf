package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAnalogIn;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.comm.naming.MqttTinkerForgeRealm;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class Voltmeter implements DeviceFactory {

	private Logger logger = LoggerFactory.getLogger(Voltmeter.class);
	@Value("${tinkerforge.voltmeter.callbackperiod?:100}")
	private long callbackperiod;
	@Value("${tinkerforge.voltmeter.topic?:'pressure'}")
	private String topic;

	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@Autowired
	private DeviceFactoryRegistry registry;
	@Autowired
	private MqttTinkerForgeRealm realm;

	@PostConstruct
	public void init() {
		registry.registerDeviceFactory(BrickletAnalogIn.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletAnalogIn sensor = new BrickletAnalogIn(uid, ipcon);
		sensor.addVoltageListener((distance) -> {
			sender.sendMessage(realm.getTopic(uid) + topic, String.valueOf(distance));
		});
		try {
			sensor.setVoltageCallbackPeriod(realm.getCallback(uid, callbackperiod));

		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
		logger.info("Voltmeter with uid {} initialized", uid);
	}
}
