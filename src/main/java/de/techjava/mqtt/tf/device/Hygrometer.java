package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.comm.naming.MqttTinkerForgeRealm;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class Hygrometer implements DeviceFactory {

	private Logger logger = LoggerFactory.getLogger(DistanceIRMeter.class);
	@Value("${tinkerforge.humidity.callbackperiod?: 10000}")
	private long callbackperiod;
	@Value("${tinkerforge.humidity.topic?:humidity}")
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
		registry.registerDeviceFactory(BrickletHumidity.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletHumidity sensor = new BrickletHumidity(uid, ipcon);
		sensor.addHumidityListener((distance) -> {
			sender.sendMessage(realm.getTopic(uid) + topic, String.valueOf(distance));
		});
		try {
			sensor.setHumidityCallbackPeriod(realm.getCallback(uid, callbackperiod));
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
		logger.info("Hygrometer with uid {} initialized", uid);
	}
}
