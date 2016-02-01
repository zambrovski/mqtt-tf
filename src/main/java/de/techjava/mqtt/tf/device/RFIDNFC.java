package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletNFCRFID;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.comm.naming.MqttTinkerForgeRealm;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;

@Component
public class RFIDNFC implements DeviceFactory {

    private Logger logger = LoggerFactory.getLogger(RFIDNFC.class);

    @Value("${tinkerforge.rfidnfc.topic?:rfidnfc}")
    private String topic;

    @Autowired
    private IPConnection ipcon;
    @Autowired
    private MqttSender sender;
    @Autowired
    private DeviceFactoryRegistry registry;
    @Autowired
    private MqttTinkerForgeRealm realm;

    @PostConstruct
    public void init() {
        registry.registerDeviceFactory(BrickletTemperature.DEVICE_IDENTIFIER, this);
    }

    /**
     * For the scan loop we remember the tag type we are scanning for.
     */
    private short currentTagType = 0;

    @Override
    public void createDevice(String uid) {
        try {
            final BrickletNFCRFID sensor = new BrickletNFCRFID(uid, ipcon);

            sensor.addStateChangedListener((state, idle) -> {
                /**
                 * Scan for Tags and send Messages for every detected ID.
                 */
                try {
                    if (idle) {
                        currentTagType = (short)((currentTagType + 1) % 3);
                        sensor.requestTagID(currentTagType);
                    }
                    if (state == BrickletNFCRFID.STATE_REQUEST_TAG_ID_READY) {
                        BrickletNFCRFID.TagID tagId = sensor.getTagID();
                        logger.debug("RFID Tag found {}", tagId);

                        // Convert to HEX String
                        StringBuilder tagIdBuilder = new StringBuilder();
                        for (int i = 0; i < tagId.tidLength; i++) {
                            tagIdBuilder.append(Integer.toHexString(tagId.tid[i]));
                        }
                        sender.sendMessage(realm.getTopic(uid) + topic, tagIdBuilder.toString());

                    } else if ((state & (1 << 6)) == (1 << 6)) {
                        // All errors have bit 6 set
                        logger.warn("RFIDNFC Error {}.", state);
                    }
                } catch (Exception e) {
                    logger.error("Exception Reading RFIDNFC-Tag.", e);
                }
            });

            // Start scan loop
            sensor.requestTagID(BrickletNFCRFID.TAG_TYPE_MIFARE_CLASSIC);

            logger.info("RFIDNFC uid {} initialized", uid);
        } catch (Exception e) {
            logger.error("Error initializing RFIDNFC.", e);
        }
    }

}
