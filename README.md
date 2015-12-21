# MQTT Tinkerforge Bridge

This project allows to bind TinkerForge sensors and actuators using MQTT to a central broker. 
The application is built using Spring Boot and is currently in a playground phase.

## Installation and usage

### Installation 

In order to build the software, please run `mvn clean package` from command line (you will require Apache Maven).

### Configuration

For configuration, make sure to copy the `application.properties` from `docs` folder and adjust the configuration.

#### General configuration

In order to connect to the TinkerForge MasterBrick, the connection needs to be specified.
Please use the following two values to configure the connection (the specified values are default):

    #
    # TF connection
    # 
    tinkerforge.host=localhost
    tinkerforge.port=4223
    
The sensor values are delivered to a MQTT broker, which has to be configured.

    #
    # MQTT connection
    #
    mqtt.broker=tcp://localhost:1883
    mqtt.client.id=localtest
    mqtt.qos=2

#### Configuring MQTT delivery

The MQTT protocol allows to create topics in a special way, so MQTT selectors (# and +) can be applied. In doing so, every MQTT sensor setup has a naming scheme which can be applied to the devices delivering the events. Usually this naming scheme is establishing a topology (physical or logical). The configuration is divided in three parts:

- Topic prefix of the entire application
- Topic segment for device
- Topic segment for measurement

The resulting topic is created by a concatenation of all three segments. The application topic prefix is configured by the property `mqtt.topic.prefix`. The device topic segment is configured using properties of scheme `<uid>.topic=cellar` where the `<uid>` is denoting the device UID of the bricklet. It is possible to configure multiple devices to point to the same topic. e.G.:

    mqtt.topic.prefix=testsetup/
    aDc.topic=cellar
    hgY.topic=cellar
    dfH.topic=cellar
    
In this setup, the three devices with given UIDs are located in cellar and deliver measurements to the same topic `testsetup/cellar`. The measurement topic is configured usgin the device specific setting (see next section).

#### Device configuration

Currently, the following sensor types are supported:
 - Ambient light
 - Barometer
 - Distance Infra-Red
 - Distance Ultra-Sound
 - Hygrometer
 - LCD 20x4 Buttons
 - Scale
 - Thermometer
 - Thermometer Infra-Red
 - Voltmeter
 
In addition, the following actuators are implemented:
 - LCD 20x4 Text and Backlight

In general, you can configure every value sensor and set its value read timeout (`callbackperiod`) and the target topic (`topic`) based on the device type.

    #
    # Example ambient light
    # 
    tinkerforge.ambilight.topic=illuminance
    tinkerforge.ambilight.callbackperiod=10000

This will configure the ambient light to be checked every 10 seconds and deliver the value to `testsetup/cellar/illuminance`. In particular cases, it is important to configure a single device (not the device type). This can be done in a similar way, how the device segment is set.
     
    #
    # Example ambient light
    # 
    aDc.callbackperiod=10000




### Run

To run the software just run `java -jar <mqtt-tinkerforge-boot-version.jar>