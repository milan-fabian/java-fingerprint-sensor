# java-fingerprint-sensor
Java library for interfacing with Adafruit fingerprint sensor/reader ZFM-20 (https://github.com/adafruit/Adafruit-Fingerprint-Sensor-Library/, https://www.adafruit.com/product/751) through serial interface. Also works with R30x fingerprint sensor (e.g. from Aliexpress).

Tested on Windows (with COMx) and Linux (with /dev/ttyUSBx), also on Raspberry Pi.

**Pull requests are always welcome**

## Sample usage

```java
// Connect (sensor is connected through UART to USB converter)
FingerprintSensor sensor = new AdafruitSensor("/dev/ttyUSB0");
sensor.connect();

// Optional: upload all models of fingerprints to sensor
// The sensor has internal non-volatile memory in which it has all models 
// saved, but I recommend storing models also elsewhere (e.g. database), 
// in case of technical failure.
for (Map.Entry<Integer, byte[]> fingerModel : fingerModels) {
	sensor.saveModel(fingerModel.getValue(), fingerModel.getKey());
}

// In loop
if (sensor.hasFingerprint()) {
	// Finger is on sensor
	Integer fingerId = sensor.searchFingerprint();
	if (fingerId != null) { // Already known fingerprint 
		System.out.println("Scanned fingerprint with ID " + fingerId);
	} else { // New fingerprint
		// Release finger from sensor ...
		while (sensor.hasFingerprint()) {
			Thread.sleep(50);
		}
		// ... and put it back on (model has to be calculated from two images)
		byte[] model = null;
		while (model = sensor.createModel() == null) {
			Thread.sleep(50);
		}
		sensor.saveStoredModel(personId);
		// Optional: store model also eslewhere
	}
}
```

For additional documentation, please see javadocs.

## TODO 

Change the API to asynchronous callbacks