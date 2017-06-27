package sk.mimac.fingerprint;

/**
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
