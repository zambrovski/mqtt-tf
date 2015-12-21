package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.comm.naming.MqttTinkerForgeRealm;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class ThermometerIR implements DeviceFactory {

	private Logger logger = LoggerFactory.getLogger(ThermometerIR.class);
	@Value("${tinkerforge.thermometer.ir.ambient.callbackperiod?: 10000}")
	private long callbackperiodAmbient;
	@Value("${tinkerforge.thermometer.ir.ambient.topic?:temperatur}")
	private String topicAmbient;
	@Value("${tinkerforge.thermometer.ir.object.callbackperiod?: 10000}")
	private long callbackperiodObject;
	@Value("${tinkerforge.thermometer.ir.object.topic?:temperatur}")
	private String topicObject;

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
		registry.registerDeviceFactory(BrickletTemperatureIR.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletTemperatureIR sensor = new BrickletTemperatureIR(uid, ipcon);
		sensor.addAmbientTemperatureListener((temp) -> {
			sender.sendMessage(realm.getTopic(uid) + topicAmbient, String.valueOf(temp));
		});
		sensor.addObjectTemperatureListener((temp) -> {
			sender.sendMessage(realm.getTopic(uid) + topicObject, String.valueOf(temp));
		});
		try {
			sensor.setAmbientTemperatureCallbackPeriod(realm.getCallback(uid + ".ambient", callbackperiodAmbient));
			sensor.setObjectTemperatureCallbackPeriod(realm.getCallback(uid + ".object", callbackperiodObject));
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
		logger.info("Thermometer IR uid {} initialized", uid);
	}

}
