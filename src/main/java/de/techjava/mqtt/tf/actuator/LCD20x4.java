package de.techjava.mqtt.tf.actuator;

import java.util.Objects;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttCallbackAdapter;
import de.techjava.mqtt.tf.comm.MqttReceiver;
import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.bricklet.lcd.uid")
public class LCD20x4 extends MqttCallbackAdapter {

	private static final String PREFIX = "lcd/";

	private Logger logger = LoggerFactory.getLogger(LCD20x4.class);
	@Autowired
	private IPConnection ipcon;
	@Autowired
	private MqttReceiver receiver;
	@Autowired
	private MqttSender sender;
	@TinkerForgeUid
	private String uid;

	private BrickletLCD20x4 lcd;

	@PostConstruct
	public void init() {
		Objects.requireNonNull(uid, "UID must not be null");
		Objects.requireNonNull(uid, "IP Connection must not be null");
		lcd = new BrickletLCD20x4(uid, ipcon);
		receiver.addListener(PREFIX + "text1", this);
		receiver.addListener(PREFIX + "text2", this);
		receiver.addListener(PREFIX + "text3", this);
		receiver.addListener(PREFIX + "text4", this);
		receiver.addListener(PREFIX + "light", this);
		logger.info("LCD initilized");
		lcd.addButtonPressedListener((button) -> {
			sender.sendMessage("button/" + button, Boolean.TRUE.toString());
		});
		lcd.addButtonReleasedListener((button) -> {
			sender.sendMessage("button/" + button, Boolean.FALSE.toString());
		});
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws Exception {
		final String value = new String(message.getPayload());
		String effectiveTopic = topic.substring(topic.indexOf(PREFIX) + PREFIX.length());
		switch (effectiveTopic) {
		case "light":
			boolean light = Boolean.parseBoolean(value);
			if (light) {
				lcd.backlightOn();
			} else {
				lcd.backlightOff();
			}
			break;
		case "text1":
			setText(0, value);
			break;
		case "text2":
			setText(1, value);
			break;
		case "text3":
			setText(2, value);
			break;
		case "text4":
			setText(3, value);
			break;
		default:
			logger.info("Skipping {}", effectiveTopic);
		}
	}

	/**
	 * Writes text to LCD.
	 * 
	 * @param line
	 *            0-3
	 * @param text
	 *            text to write.
	 */
	private void setText(int line, String text) {
		try {
			lcd.writeLine((short) line, (short) 0, text);
		} catch (TimeoutException | NotConnectedException e) {
			logger.error("Error writing text to LCD", e);
		}
	}
}
