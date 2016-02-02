package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;

import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class ThermometerIR implements DeviceFactory {

	private Logger logger = LoggerFactory.getLogger(ThermometerIR.class);
	@Value("${tinkerforge.thermometer.ir.ambient.callbackperiod?: 10000}")
	private long callbackperiodAmbient;
	@Value("${tinkerforge.thermometer.ir.ambient.topic?:temperature}")
	private String topicAmbient;
	@Value("${tinkerforge.thermometer.ir.object.callbackperiod?: 10000}")
	private long callbackperiodObject;
	@Value("${tinkerforge.thermometer.ir.object.topic?:temperature_object}")
	private String topicObject;

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
		registry.registerDeviceFactory(BrickletTemperatureIR.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(String uid) {
		BrickletTemperatureIR sensor = new BrickletTemperatureIR(uid, ipcon);
		sensor.addAmbientTemperatureListener((temp) -> {
			sender.sendMessage(realm.getTopic(uid) + topicAmbient, (((Short)temp).doubleValue())/10.0);
		});
		sensor.addObjectTemperatureListener((temp) -> {
		    logger.debug("Object Temperature {}", temp);
			sender.sendMessage(realm.getTopic(uid) + topicObject, (((Short)temp).doubleValue())/10.0);
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
