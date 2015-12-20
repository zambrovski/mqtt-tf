package de.techjava.mqtt.tf.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Filters TinkerForge Components.
 * <p>
 * This condition checks if the UID property is pointing to a value that is
 * defined in property file.
 * </p>
 * 
 * @author Simon Zambrovski
 */
public class TinkerForgeCondition implements Condition {

	private static final Logger LOGGER = LoggerFactory.getLogger(TinkerForgeCondition.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		LOGGER.debug("Verifying {}", metadata.toString());
		final Map<String, Object> annotationAttributes = metadata
				.getAnnotationAttributes(TinkerForgeComponent.class.getName());
		if (annotationAttributes != null && !annotationAttributes.isEmpty()) {
			final String uidProperty = (String) annotationAttributes.get("uidProperty");
			LOGGER.debug("Checking property {}", uidProperty);
			if (uidProperty != null && context.getEnvironment().getProperty(uidProperty) != null) {
				return true;
			}
		}
		return false;
	}

}
