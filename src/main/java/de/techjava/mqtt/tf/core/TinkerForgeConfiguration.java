package de.techjava.mqtt.tf.core;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.IPConnectionBase;

import de.techjava.mqtt.tf.device.MasterBrick;

/**
 * Main configuration of TF components, factories registry and connection.
 * 
 * @author Simon Zambrovski
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = { Enumerator.class, MasterBrick.class })
public class TinkerForgeConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(TinkerForgeConfiguration.class);

	@Value("${tinkerforge.host}")
	private String host;
	@Value("${tinkerforge.port}")
	private int port;

	private IPConnection ipConnection;

	@Bean(destroyMethod = "disconnect")
	@DependsOn("registry")
	public IPConnection getIPConnection(final DeviceFactoryRegistry registry) {
		if (ipConnection == null) {
			ipConnection = new IPConnection();

			ipConnection.addDisconnectedListener((disconnectReason) -> {
				switch (disconnectReason) {
				case IPConnectionBase.DISCONNECT_REASON_ERROR:
					logger.info("Closing TinkerForge connection because of error.");
					break;
				case IPConnectionBase.DISCONNECT_REASON_REQUEST:
					logger.info("Disconnecting TinkerForge connection.");
					break;
				case IPConnectionBase.DISCONNECT_REASON_SHUTDOWN:
					logger.info("Shutting down TinkerForge connection.");
					break;
				}
			});
			ipConnection.addEnumerateListener((uid, connectedUid, position, hardwareVersion, firmwareVersion,
					deviceIdentifier, enumerationType) -> {
				switch (enumerationType) {
				case IPConnection.ENUMERATION_TYPE_AVAILABLE:
					final List<DeviceFactory> factories = registry.getDeviceFactory(deviceIdentifier);
					if (factories != null) {
					    for (DeviceFactory factory : factories) {
					        factory.createDevice(uid);
                        }
						
					} else {
						logger.error("No factory found for device type {}", deviceIdentifier);
					}
					break;
				case IPConnection.ENUMERATION_TYPE_CONNECTED:
					logger.info("Connected new Brick with uid {}", uid);
					break;
				case IPConnection.ENUMERATION_TYPE_DISCONNECTED:
					break;
				}
			});

			ipConnection.setAutoReconnect(true);
		}
		// establish connection.
		connect();

		return ipConnection;
	}

	/**
	 * Creates the device factory registry.
	 * 
	 * @return
	 */
	@Bean(name = "registry")
	public DeviceFactoryRegistry getRegistry() {
		return new DeviceFactoryRegistry();
	}

	/**
	 * Create the connection.
	 */
	private void connect() {
		try {
			logger.info("Connecting to TinkerForge: {}:{}...", host, port);
			ipConnection.connect(host, port);
			logger.info("Connection established.");
		} catch (AlreadyConnectedException | IOException e) {
			logger.error("Error establishing connection", e);
			System.exit(1);
		}
	}
}