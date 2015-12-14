package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;

@Component
public class Barometer {

    private Logger logger = LoggerFactory.getLogger(Barometer.class);
    @Autowired
    private IPConnection ipcon;

    @Autowired
    private MqttSender sender;

    @Value("${tinkerforge.bricklet.barometer.uid}")
    private String uid;

    @Value("${tinkerforge.bricklet.barometer.callbackperiod}")
    private long callbackperiod;

    private BrickletBarometer barometer;

    @PostConstruct
    public void init() {
        barometer = new BrickletBarometer(uid, ipcon);
        barometer.addAirPressureListener((airPressure) -> {
            sender.sendMessage("pressure", String.valueOf(airPressure));
        });
        logger.info("Barometer initilized");
        try {
            barometer.setAirPressureCallbackPeriod(callbackperiod);
        } catch (TimeoutException | NotConnectedException e) {
            logger.error("Error setting callbackperiod", e);
        }
    }
}
