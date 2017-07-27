package sk.mimac.fingerprint;

/**
 * List of used keys (in case you would like to translate the message):
 * <ul>
 * <li>sensor.cant.connect - Can't open serial port</li>
 * <li>sensor.bad.data - Received incorrect data</li>
 * <li>sensor.cant.write - Can't write to serial port</li>
 * <li>sensor.cant.read - Can't read from serial port</li>
 * <li>sensor.not.responding - Received no response for sent request</li>
 * </ul>
 * 
 * @author Milan Fabian
 */
public class FingerprintException extends Exception {

    private final String key;

    public FingerprintException(String message, String key) {
        super(message);
        this.key = key;
    }

    public FingerprintException(String message, String key, Throwable cause) {
        super(message, cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
