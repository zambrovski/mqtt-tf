package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickMaster;
import com.tinkerforge.IPConnection;

@Component
public class MasterBrick {

	private static final Logger logger = LoggerFactory.getLogger(MasterBrick.class);

	@Autowired
	private IPConnection ipcon;
	private BrickMaster master;

	@Value("${tinkerforge.master.uid}")
	private String uid;

	@PostConstruct
	public void init() {
		master = new BrickMaster(uid, ipcon);
		logger.info("Master Brick initilized");
	}

}
