package de.techjava.mqtt.tf.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Annotation for TinkerForge component.
 * 
 * @author Simon Zambrovski
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Conditional(value = TinkerForgeCondition.class)
public @interface TinkerForgeComponent {
	/**
	 * Defines the attribute to read UID from configuration.
	 * 
	 * @return expression pointing to uid property.
	 */
	String uidProperty();
}
