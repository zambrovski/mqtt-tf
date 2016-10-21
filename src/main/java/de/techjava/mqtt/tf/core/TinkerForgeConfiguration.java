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
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import com.tinkerforge.IPConnectionBase;

import de.techjava.mqtt.tf.device.MasterBrick;

/**
 * Main configuration of TF components, factories registry and connection.
 * 
 * @author Simon Zambrovski
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = { Enumerator.class, MasterBrick.class })
public class TinkerForgeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinkerForgeConfiguration.class);

    @Value("${tinkerforge.host}")
    private String host;
    @Value("${tinkerforge.port}")
    private int port;

    private IPConnection ipConnection;

    /**
     * Retrieves the IP Connection to the TF Master Brick.
     * 
     * @param registry
     *            registry to register devices.
     * @return configured connection.
     */
    @SuppressWarnings("unchecked")
    @Bean(destroyMethod = "disconnect")
    @DependsOn("registry")
    public IPConnection getIPConnection(final DeviceFactoryRegistry registry) {
        if (ipConnection == null) {
            ipConnection = new IPConnection();

            ipConnection.addDisconnectedListener((disconnectReason) -> {
                switch (disconnectReason) {
                case IPConnectionBase.DISCONNECT_REASON_ERROR:
                    LOGGER.info("Closing TinkerForge connection because of error.");
                    break;
                case IPConnectionBase.DISCONNECT_REASON_REQUEST:
                    LOGGER.info("Disconnecting TinkerForge connection.");
                    break;
                case IPConnectionBase.DISCONNECT_REASON_SHUTDOWN:
                    LOGGER.info("Shutting down TinkerForge connection.");
                    break;
                }
            });
            ipConnection.addEnumerateListener((uid, connectedUid, position, hardwareVersion, firmwareVersion, deviceIdentifier, enumerationType) -> {
                switch (enumerationType) {
                case IPConnection.ENUMERATION_TYPE_AVAILABLE:

                    final DeviceFactory<?> factory = registry.getDeviceFactory(deviceIdentifier);
                    if (factory != null) {
                        final Device device = factory.createDevice(uid);
                        registry.createdDevice(uid, device);

                        // Configure the device
                        final List<DeviceController<?>> controllers = registry.getDeviceControllers(deviceIdentifier);
                        if (controllers != null) {
                            for (@SuppressWarnings("rawtypes")
                            final DeviceController deviceController : controllers) {
                                LOGGER.info("Added Device Controller for uid {}", uid);
                                deviceController.setupDevice(uid, device);
                            }
                        } else {
                            LOGGER.info("No controllers (listener configurations) found for device type {}", deviceIdentifier);
                        }
                    } else {
                        LOGGER.error("No factory found for device type {}", deviceIdentifier);
                    }
                    break;
                case IPConnection.ENUMERATION_TYPE_CONNECTED:
                    LOGGER.info("Connected new Brick with uid {}", uid);
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
     * @return device factory registry.
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
            LOGGER.info("Connecting to TinkerForge: {}:{}...", host, port);
            ipConnection.connect(host, port);
            LOGGER.info("Connection established.");
        } catch (AlreadyConnectedException | IOException e) {
            LOGGER.error("Error establishing connection", e);
            System.exit(1);
        }
    }
}