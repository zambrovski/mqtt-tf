package de.techjava.mqtt.tf.comm;

import java.util.Date;

public class Message {

    private Object value;
    private Date _timestamp;

    public Message(Object value) {
        this.value = value;
        this._timestamp = new Date();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date get_timestamp() {
        return _timestamp;
    }

    public void set_timestamp(Date _timestamp) {
        this._timestamp = _timestamp;
    }
}
