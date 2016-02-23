package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class ThermometerIR implements DeviceFactory<BrickletTemperatureIR>, DeviceController<BrickletTemperatureIR> {

	private Logger logger = LoggerFactory.getLogger(ThermometerIR.class);
	@Value("${tinkerforge.thermometer.ir.ambient.callbackperiod?: 10000}")
	private long callbackperiodAmbient;
	@Value("${tinkerforge.thermometer.ir.ambient.topic?:temperature}")
	private String topicAmbient;
	@Value("${tinkerforge.thermometer.ir.object.callbackperiod?: 10000}")
	private long callbackperiodObject;
	@Value("${tinkerforge.thermometer.ir.object.topic?:temperature_object}")
	private String topicObject;
	@Value("${tinkerforge.thermometer.ir.object.disabled:no}")
	private String disabledObject;
	@Value("${tinkerforge.thermometer.ir.ambient.disabled:no}")
	private String disabledAmbient;

	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@Autowired
	private DeviceFactoryRegistry registry;
	@Autowired
	private EnvironmentHelper envHelper;

	@PostConstruct
	public void init() {
		registry.registerDeviceFactory(BrickletTemperatureIR.DEVICE_IDENTIFIER, this);
		registry.registerDeviceController(BrickletTemperatureIR.DEVICE_IDENTIFIER, this);
	}

	@Override
	public BrickletTemperatureIR createDevice(String uid) {
		BrickletTemperatureIR sensor = new BrickletTemperatureIR(uid, ipcon);
		return sensor;
	}

	@Override
	public void setupDevice(final String uid, final BrickletTemperatureIR sensor) {

		boolean enableAmbient = !envHelper.isDisabled(uid + ".ambient", disabledAmbient);
		boolean enableObject = !envHelper.isDisabled(uid + ".object", disabledObject);

		logger.info("Enablement {} {}", enableAmbient, enableObject);

		if (enableAmbient) {
			sensor.addAmbientTemperatureListener((temp) -> {
				sender.sendMessage(envHelper.getTopic(uid) + topicAmbient, (((Short) temp).doubleValue()) / 10.0);
			});
		} else {
			logger.info("Ambient temperature IR listener disabled");
		}

		if (enableObject) {
			sensor.addObjectTemperatureListener((temp) -> {
				logger.debug("Object Temperature {}", temp);
				sender.sendMessage(envHelper.getTopic(uid) + topicObject, (((Short) temp).doubleValue()) / 10.0);
			});
		} else {
			logger.info("Object temperature listner disabled");
		}
		try {
			if (enableAmbient) {
				sensor.setAmbientTemperatureCallbackPeriod(
						envHelper.getCallback(uid + ".ambient", callbackperiodAmbient));
			}
			if (enableObject) {
				sensor.setObjectTemperatureCallbackPeriod(envHelper.getCallback(uid + ".object", callbackperiodObject));
			}
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error setting callback period", e);
		}
		logger.info("Thermometer IR uid {} initialized", uid);
	}

}
