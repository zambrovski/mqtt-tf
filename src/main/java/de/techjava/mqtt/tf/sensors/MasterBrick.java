package de.techjava.mqtt.tf.sensors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tinkerforge.BrickMaster;
import com.tinkerforge.IPConnection;

import de.techjava.mqtt.tf.util.TinkerForgeComponent;
import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;
import de.techjava.mqtt.tf.util.TinkerForgeUid;

@TinkerForgeComponent(uidProperty = "tinkerforge.master.uid")
public class MasterBrick {

	private static final Logger logger = LoggerFactory.getLogger(MasterBrick.class);

	@Autowired
	private IPConnection ipcon;
	@Autowired
	private TinkerForgeInitializerAspect initializer;

	private BrickMaster master;

	@TinkerForgeUid
	private String uid;

	@PostConstruct
	public void init() {
		initializer.initalizeComponent(this);
		master = new BrickMaster(uid, ipcon);
		logger.info("Master Brick initilized");
	}

}
