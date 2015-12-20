package de.techjava.mqtt.tf.sensors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.techjava.mqtt.tf.Starter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Starter.class)
public class ThermometerTest {

	@Autowired
	Thermometer thermo;

	@Test
	public void conditionalTesting() {
		thermo.toString();
	}
}
