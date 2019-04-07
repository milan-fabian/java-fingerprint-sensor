package sk.mimac.fingerprint;

import java.io.Closeable;

/**
 * General interface for communication with fingerprint sensor.
 *
 * @author Milan Fabian
 */
public interface FingerprintSensor extends Closeable {

    /**
     * Removes all saved fingerprint models from sensor's internal memory.
     *
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    void clearAllSaved() throws FingerprintException;

    /**
     * Connect to the sensor. Must be called before calling any other method.
     *
     * @throws FingerprintException if sensor is unavailable
     */
    void connect() throws FingerprintException;

    /**
     * Create model of fingerprint placed on sensor. The same finger must have
     * been previously checked by {@link #hasFingerprint() hasFingerprint}
     * method.
     *
     * @return model of fingerprint or null if no fingerprint is placed on
     * sensor
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    byte[] createModel() throws FingerprintException;

    /**
     * Checks if any finger is placed on the sensor and if yes, saves the
     * fingerprint to sensor's internal cache.
     *
     * @return true if finger is placed on sensor, false otherwise
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    boolean hasFingerprint() throws FingerprintException;

    /**
     * Saves previously created fingerprint model to sensor's internal memory.
     *
     * @param model of fingerprint
     * @param number position in sensor's internal memory
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    void saveModel(byte[] model, int number) throws FingerprintException;

    /**
     * Save fingerprint model which was created by
     * {@link #createModel() createModel} method to sensor's internal memory.
     *
     * @param number position in sensor's internal memory
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    void saveStoredModel(int number) throws FingerprintException;

    /**
     * Search sensor's internal memory for match with finger on which
     * {@link #hasFingerprint() hasFingerprint} was called.
     *
     * @return position of matched fingerprint in sensor's internal memory or
     * null if fingerprint doesn't match any stored fingerprint
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    Integer searchFingerprint() throws FingerprintException;

    /**
     * Read basic parameters from the fingerprint sensor.
     * 
     * @return wrapper for parameters
     * @throws FingerprintException if there is problem with communication to
     * sensor
     */
    SensorParameters readParameters() throws FingerprintException;

    /**
     * Set security level for matching fingerprints
     * 
     * @param securityLevel from 1 (lowest security) to 5 (highest secutity)
     * @throws FingerprintException if there is problem with communication to
     * sensor 
     */
    void setSecurityLevel(int securityLevel) throws FingerprintException;
}
