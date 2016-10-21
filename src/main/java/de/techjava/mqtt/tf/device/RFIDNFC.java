package de.techjava.mqtt.tf.device;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletNFCRFID;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.techjava.mqtt.tf.comm.MqttSender;
import de.techjava.mqtt.tf.core.DeviceController;
import de.techjava.mqtt.tf.core.DeviceFactory;
import de.techjava.mqtt.tf.core.DeviceFactoryRegistry;
import de.techjava.mqtt.tf.core.EnvironmentHelper;

@Component
public class RFIDNFC implements DeviceFactory<BrickletNFCRFID>, DeviceController<BrickletNFCRFID> {

    private Logger logger = LoggerFactory.getLogger(RFIDNFC.class);

    @Value("${tinkerforge.rfidnfc.topic?:rfidnfc}")
    private String topic;
    /**
     * @see {@link BrickletNFCRFID#TAG_TYPE_MIFARE_CLASSIC} {@link BrickletNFCRFID#TAG_TYPE_TYPE1} {@link BrickletNFCRFID#TAG_TYPE_TYPE2
     */
    @Value("${tinkerforge.rfidnfc.tagtyp?:0}")
    private Short tagType;

    @Value("${tinkerforge.rfidnfc.disabled?:false}")
    private String disabled;
    @Autowired
    private IPConnection ipcon;
    @Autowired
    private MqttSender sender;
    @Autowired
    private DeviceFactoryRegistry registry;
    @Autowired
    private EnvironmentHelper envHelper;

    private String lastTagId;

    @PostConstruct
    public void init() {
        registry.registerDeviceFactory(BrickletNFCRFID.DEVICE_IDENTIFIER, this);
        registry.registerDeviceController(BrickletNFCRFID.DEVICE_IDENTIFIER, this);
    }

    /**
     * For the scan loop we remember the tag type we are scanning for.
     */
    private short currentTagType = 0;

    @Override
    public BrickletNFCRFID createDevice(String uid) {
        final BrickletNFCRFID sensor = new BrickletNFCRFID(uid, ipcon);
        return sensor;
    }

    @Override
    public void setupDevice(final String uid, final BrickletNFCRFID sensor) {
        sensor.addStateChangedListener((state, idle) -> {
            logger.trace("RFID State changed {} {}", state, idle);
            /*
             * Scan for Tags and send Messages for every detected ID.
             */
            try {
                if (idle) {
                    currentTagType = (short) ((currentTagType + 1) % 3);
                    sensor.requestTagID(currentTagType);
                }
                if (state == BrickletNFCRFID.STATE_REQUEST_TAG_ID_READY) {
                    BrickletNFCRFID.TagID tagId = sensor.getTagID();
                    logger.trace("RFID Tag found {}", tagId);

                    // Convert to HEX String
                    final StringBuilder tagIdBuilder = new StringBuilder();
                    for (int i = 0; i < tagId.tidLength; i++) {
                        tagIdBuilder.append(Integer.toHexString(tagId.tid[i]));
                    }
                } else if ((state & (1 << 6)) == (1 << 6)) {
                    // All errors have bit 6 set
                    logger.trace("RFIDNFC Error {}.", state);
                }

            } catch (TimeoutException | NotConnectedException e) {
                logger.error("Exception Reading RFIDNFC-Tag.", e);
            }
        });
        logger.info("RFIDNFC uid {} initialized", uid);

        boolean enable = !envHelper.isDisabled(uid, RFIDNFC.class);
        if (enable) {
            // Start scan loop
            try {
                sensor.requestTagID(tagType);
            } catch (TimeoutException | NotConnectedException e) {
                logger.error("Error initializing RFIDNFC");
            }
        }

    }

    private void newTagIdIdentified(String uid) {
        sender.sendMessage(envHelper.getTopic(uid) + topic, lastTagId);
    }

}
