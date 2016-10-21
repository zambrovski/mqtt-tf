package de.techjava.mqtt.tf;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.skyscreamer.jsonassert.JSONAssert.*;

import java.util.HashMap;

import de.techjava.mqtt.tf.comm.MqttSender;

public class JsonSerializerTest {

    @Test
    public void serializeIntMessage() {
        String seralizeMessage = MqttSender.seralizeMessage("foo", 17);
        assertEquals("{\"value\":17}", seralizeMessage, false);
    }

    @Test
    public void serializeBooleanMessage() {
        String seralizeMessage = MqttSender.seralizeMessage("foo", true);
        assertEquals("{\"value\":true}", seralizeMessage, false);
    }

    @Test
    public void serializeStringMessage() {
        String seralizeMessage = MqttSender.seralizeMessage("foo", "bar");
        assertEquals("{\"value\":\"bar\"}", seralizeMessage, false);
    }

    @Test
    public void serializeObjectMessageWrongJson() {
        String seralizeMessage = MqttSender.seralizeMessage("foo", new Dummy("foo", 17, new HashMap<>(4)));
        assertNull(seralizeMessage);
    }

    /**
     * Class without getters/setters to create a serialization exception.
     */
    class Dummy {
        String foo;
        Integer bar;
        HashMap<String, Object> map;

        public Dummy(String foo, Integer bar, HashMap<String, Object> map) {
            this.foo = foo;
            this.bar = bar;
            this.map = map;
        }
    }
}
