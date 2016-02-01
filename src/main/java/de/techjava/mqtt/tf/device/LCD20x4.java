package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttCallbackAdapter;
import de.techjava.mqtt.tf.comm.MqttReceiver;
import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class LCD20x4 implements DeviceFactory {

	private static final Logger logger = LoggerFactory.getLogger(LCD20x4.class);
	@Value("${tinkerforge.lcd20x4.button.topic?:lcd/button}")
	private String buttonTopic;
	@Value("${tinkerforge.lcd20x4.backlight.topic?:lcd/backlight}")
	private String backlightTopic;
	@Value("${tinkerforge.lcd20x4.text1.topic?:lcd/text/1}")
	private String text1Topic;
	@Value("${tinkerforge.lcd20x4.text2.topic?:lcd/text/2}")
	private String text2Topic;
	@Value("${tinkerforge.lcd20x4.text3.topic?:lcd/text/3}")
	private String text3Topic;
	@Value("${tinkerforge.lcd20x4.text4.topic?:lcd/text/4}")
	private String text4Topic;

	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttSender sender;
	@Autowired
	private MqttReceiver receiver;
	@Autowired
	private DeviceFactoryRegistry registry;
	@Autowired
	private EnvironmentHelper realm;

	@PostConstruct
	public void init() {
		registry.registerDeviceFactory(BrickletLCD20x4.DEVICE_IDENTIFIER, this);
	}

	@Override
	public void createDevice(final String uid) {
		BrickletLCD20x4 lcd = new BrickletLCD20x4(uid, ipcon);
		MqttCallback callback = createCallback(lcd, backlightTopic, text1Topic, text2Topic, text3Topic, text4Topic);
		receiver.addListener(realm.getTopic(uid) + text1Topic, callback);
		receiver.addListener(realm.getTopic(uid) + text2Topic, callback);
		receiver.addListener(realm.getTopic(uid) + text3Topic, callback);
		receiver.addListener(realm.getTopic(uid) + text4Topic, callback);
		receiver.addListener(realm.getTopic(uid) + backlightTopic, callback);
		lcd.addButtonPressedListener((button) -> {
			sender.sendMessage(realm.getTopic(uid) + buttonTopic + "/" + button, Boolean.TRUE.toString());
		});
		lcd.addButtonReleasedListener((button) -> {
			sender.sendMessage(realm.getTopic(uid) + buttonTopic + "/" + button, Boolean.FALSE.toString());
		});
		logger.info("LCD with uid {} initilized.", uid);
	}

	/**
	 * TODO implement in a better way.
	 * 
	 * @param lcd
	 * @param backlightTopic
	 * @param text1Topic
	 * @param text2Topic
	 * @param text3Topic
	 * @param text4Topic
	 * @return
	 */
	public static MqttCallback createCallback(final BrickletLCD20x4 lcd, String backlightTopic, String text1Topic,
			String text2Topic, String text3Topic, String text4Topic) {
		MqttCallbackAdapter adapter = new MqttCallbackAdapter() {
			@Override
			public void messageArrived(final String topic, final MqttMessage message) throws Exception {

				final String value = new String(message.getPayload());
				try {
					if (topic.equals(backlightTopic)) {
						boolean lightOn = Boolean.parseBoolean(value);
						if (lightOn) {
							lcd.backlightOn();
						} else {
							lcd.backlightOff();
						}
					} else if (topic.equals(text1Topic)) {
						lcd.writeLine((short) 0, (short) 0, value);
					} else if (topic.equals(text2Topic)) {
						lcd.writeLine((short) 1, (short) 0, value);
					} else if (topic.equals(text3Topic)) {
						lcd.writeLine((short) 2, (short) 0, value);
					} else if (topic.equals(text4Topic)) {
						lcd.writeLine((short) 3, (short) 0, value);
					}
				} catch (TimeoutException | NotConnectedException e) {
					logger.error("Error writing text to LCD", e);
				}

			}
		};
		return adapter;
	}
}
