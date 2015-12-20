package de.techjava.mqtt.tf.util;

import java.lang.reflect.Field;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@Aspect
public class TinkerForgeInitializerAspect {

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(TinkerForgeInitializerAspect.class);

	@Around("@target(de.techjava.mqtt.tf.util.TinkerForgeComponent) && @annotation(javax.annotation.PostConstruct)")
	public void beforeTinkerForgeComponentPostConstruct(final ProceedingJoinPoint pjp) {
		Object bean = pjp.getTarget();
		initalizeComponent(bean);
	}

	/**
	 * TODO try to remove this component, and replace it by an aspect...
	 * 
	 * @param bean
	 *            the tinkerforge component
	 */
	public void initalizeComponent(Object bean) {
		final TinkerForgeComponent annotation = bean.getClass().getAnnotation(TinkerForgeComponent.class);
		if (annotation == null) {
			logger.warn("Nothing to initialize. The TinkerForgeComponent annotation must be present.");
			return;
		}
		final String property = annotation.uidProperty();
		final Field[] declaredFields = bean.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(TinkerForgeUid.class)) {
				field.setAccessible(true);
				try {
					final Object value = env.getProperty(property);
					field.set(bean, value);
					logger.info("Set {} to {} on {}", field.getName(), value, bean.getClass().getName());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Error setting the UID", e);
				}
			}
		}
	}

}
