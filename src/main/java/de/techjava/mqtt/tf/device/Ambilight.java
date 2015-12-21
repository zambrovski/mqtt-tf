package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.comm.naming.MqttTinkerForgeRealm;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class Ambilight implements DeviceFactory {

	private static final Logger logger = LoggerFactory.getLogger(Ambilight.class);

	@Value("${tinkerforge.ambilight.topic?:illuminance}")
	private String topic;

	@Value("${tinkerforge.ambilight.callbackperiod?:1000}")
	private long callbackperiod;

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
		registry.registerDeviceFactory(BrickletAmbientLight.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletAmbientLight bricklet = new BrickletAmbientLight(uid, ipcon);
		bricklet.addIlluminanceListener((illuminance) -> {
			sender.sendMessage(realm.getTopic(uid) + topic, String.valueOf(illuminance));
		});
		try {
			bricklet.setIlluminanceCallbackPeriod(realm.getCallback(uid, callbackperiod));
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting Illuminance Callback Period", e);
		}
		logger.info("Ambilight sensor with uid {} initialized.", uid);
	}
}
