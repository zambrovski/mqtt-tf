package de.techjava.mqtt.tf;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.IPConnectionBase;

import de.techjava.mqtt.tf.util.TinkerForgeInitializerAspect;

@Configuration
public class TinkerForgeConfiguration {

	static final Logger logger = LoggerFactory.getLogger(TinkerForgeConfiguration.class);

	@Value("${tinkerforge.host}")
	private String host;
	@Value("${tinkerforge.port}")
	private int port;

	final IPConnection ipConnection = new IPConnection();

	@Bean
	public TinkerForgeInitializerAspect getTinkerForgeInitializerAspect() {
		return new TinkerForgeInitializerAspect();
	}

	@Bean
	public IPConnection getIPConnection() {
		ipConnection.addDisconnectedListener((disconnectReason) -> {
			switch (disconnectReason) {
			case IPConnectionBase.DISCONNECT_REASON_ERROR:
				connect();
				break;
			case IPConnectionBase.DISCONNECT_REASON_REQUEST:
				break;
			case IPConnectionBase.DISCONNECT_REASON_SHUTDOWN:
				break;
			}
		});

		connect();
		return ipConnection;
	}

	/**
	 * Create the connection.
	 */
	private void connect() {
		try {
			logger.info("Connecting to TinkerForge: {}:{}...", host, port);
			ipConnection.connect(host, port);
			logger.info("Connected.");
		} catch (AlreadyConnectedException | IOException e) {
			logger.error("Error establishing connection", e);
		}
	}

}
