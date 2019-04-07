package sk.mimac.fingerprint.adafruit;

import sk.mimac.fingerprint.FingerprintSensor;
import gnu.io.NRSerialPort;
import java.io.*;
import java.util.*;
import org.slf4j.*;
import sk.mimac.fingerprint.FingerprintException;
import sk.mimac.fingerprint.SensorParameters;
import static sk.mimac.fingerprint.adafruit.AdafruitConstants.*;

/**
 * For fingerprint sensor ZFM-20. More info at
 * https://github.com/adafruit/Adafruit-Fingerprint-Sensor-Library/.
 *
 * @author Milan Fabian
 */
public class AdafruitSensor implements FingerprintSensor {

    private static final Logger logger = LoggerFactory.getLogger(AdafruitSensor.class);

    private final NRSerialPort serial;
    private DataInputStream input;
    private OutputStream output;

    /**
     * Construct sensor class with default baudRate (57600).
     *
     * @param serialPort where the fingerprint sensor is connected
     */
    public AdafruitSensor(String serialPort) {
        this(serialPort, 57600);
    }

    /**
     * Construct sensor class.
     *
     * @param serialPort where the fingerprint sensor is connected
     * @param baudRate of the sensor's serial interface (default is 57600)
     */
    public AdafruitSensor(String serialPort, int baudRate) {
        serial = new NRSerialPort(serialPort, baudRate);
    }

    @Override
    public void connect() throws FingerprintException {
        if (!serial.connect()) {
            throw new FingerprintException("Can't connect to fingerprint sensor", "sensor.cant.connect");
        }
        input = new DataInputStream(serial.getInputStream());
        output = serial.getOutputStream();
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_VERIFYPASSWORD, 0, 0, 0, 0});
        byte[] reply = getReply();
        if ((reply[0] != FINGERPRINT_ACKPACKET) || (reply[1] != FINGERPRINT_OK)) {
            throw new FingerprintException("Can't initialize fingerprint sensor, reply is: " + bytesToHex(reply), "sensor.cant.connect");
        }
    }

    @Override
    public void close() throws IOException {
        if (serial.isConnected()) {
            try {
                serial.disconnect();
            } catch (Exception ex) {
                logger.warn("Error while disconnecting from fingerprint sensor: " + ex);
            }
        }
    }

    @Override
    public boolean hasFingerprint() throws FingerprintException {
        if (!getImage()) {
            return false;
        }
        return image2tz((byte) 1);
    }

    @Override
    public Integer searchFingerprint() throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_HISPEEDSEARCH, 0x01, 0x00, 0x00, 0x01, 0x00});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        switch (reply[1]) {
            case FINGERPRINT_OK:
                int fingerID = reply[2] & 0xFF;
                fingerID <<= 8;
                fingerID |= reply[3] & 0xFF;

                int confidence = reply[4] & 0xFF;
                confidence <<= 8;
                confidence |= reply[5] & 0xFF;
                logger.debug("Found fingerprint with number " + fingerID + " with confidence " + confidence);
                return fingerID;
            case FINGERPRINT_NOTFOUND:
                return null;
            default:
                throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    @Override
    public byte[] createModel() throws FingerprintException {
        if (!getImage()) {
            return null;
        }
        if (!image2tz((byte) 2)) {
            return null;
        }
        regModel();
        return upload();
    }

    @Override
    public void clearAllSaved() throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_EMPTY});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET || reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    @Override
    public void saveModel(byte[] model, int number) throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_DOWNLOAD, 0x01});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET || reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        writePacket(FINGERPRINT_DATAPACKET, Arrays.copyOfRange(model, 0, 128));
        writePacket(FINGERPRINT_DATAPACKET, Arrays.copyOfRange(model, 128, 256));
        writePacket(FINGERPRINT_DATAPACKET, Arrays.copyOfRange(model, 256, 384));
        writePacket(FINGERPRINT_ENDDATAPACKET, Arrays.copyOfRange(model, 384, 512));
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_STORE, 0x01, (byte) (number >> 8), (byte) (number & 0xFF)});
        reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET || reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    @Override
    public void saveStoredModel(int number) throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_STORE, 0x01, (byte) (number >> 8), (byte) (number & 0xFF)});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET || reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    @Override
    public SensorParameters readParameters() throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_READ_SYS_PARAM});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET && reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        SensorParameters parameters = new SensorParameters();
        parameters.setLibrarySize((reply[6] & 0xFF) << 8 | reply[7] & 0xFF);
        parameters.setSecurityLevel(reply[9]);
        return parameters;
    }

    @Override
    public void setSecurityLevel(int securityLevel) throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_SET_SYS_PARAM, 0x05, (byte) securityLevel});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET && reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    private boolean getImage() throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_GETIMAGE});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        return reply[1] == FINGERPRINT_OK;
    }

    private boolean image2tz(byte slot) throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_IMAGE2TZ, slot});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        return reply[1] == FINGERPRINT_OK;
    }

    private byte[] upload() throws FingerprintException {
        // Get model
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_UPLOAD, 0x01});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET || reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        // Read model
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (reply[0] != FINGERPRINT_ENDDATAPACKET) {
            reply = getReply();
            if (reply[0] != FINGERPRINT_DATAPACKET && reply[0] != FINGERPRINT_ENDDATAPACKET) {
                throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
            }
            outputStream.write(reply, 1, reply.length - 1);
        }
        return outputStream.toByteArray();
    }

    private void regModel() throws FingerprintException {
        writePacket(FINGERPRINT_COMMANDPACKET, new byte[]{FINGERPRINT_REGMODEL});
        byte[] reply = getReply();
        if (reply[0] != FINGERPRINT_ACKPACKET) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
        if (reply[1] == FINGERPRINT_ENROLLMISMATCH) {
            throw new FingerprintException("Fingerprints doesn't match", "sensor.fingerprints.doesnt.match");
        }
        if (reply[1] != FINGERPRINT_OK) {
            throw new FingerprintException("Got wrong data from fingerprint sensor: " + bytesToHex(reply), "sensor.bad.data");
        }
    }

    private void writePacket(byte packetType, byte[] packet) throws FingerprintException {
        int length = packet.length + 2;
        byte[] data = new byte[packet.length + 11];

        data[0] = (byte) (FINGERPRINT_STARTCODE >> 8);
        data[1] = (byte) FINGERPRINT_STARTCODE;
        data[2] = 0xFFFFFFFF >> 24;
        data[3] = 0xFFFFFFFF >> 16;
        data[4] = 0xFFFFFFFF >> 8;
        data[5] = 0xFFFFFFFF;
        data[6] = packetType;
        data[7] = (byte) (length >> 8);
        data[8] = (byte) length;

        int sum = (length >> 8) + (length & 0xFF) + packetType;
        for (int i = 0; i < packet.length; i++) {
            data[9 + i] = packet[i];
            sum += ((int) packet[i]) & 0xFF;
        }

        data[9 + packet.length] = (byte) (sum >> 8);
        data[10 + packet.length] = (byte) (sum & 0xFF);
        try {
            output.write(data);
            output.flush();
        } catch (IOException ex) {
            throw new FingerprintException("Can't write data to sensor", "sensor.cant.write", ex);
        }
    }

    private byte[] getReply() throws FingerprintException {
        List<Byte> reply = new ArrayList<>(10);
        int index = 0;
        int length = 0;
        try {
            while (true) {
                waitForInputAvailable(index);
                byte data = (byte) input.read();
                if ((index == 0) && (data != (byte) (FINGERPRINT_STARTCODE >> 8))) {
                    continue;
                }
                reply.add(data);

                if (index == 8) {
                    if ((reply.get(0) != (byte) (FINGERPRINT_STARTCODE >> 8)) || (reply.get(1) != (byte) FINGERPRINT_STARTCODE)) {
                        throw new FingerprintException("Bad packet", "sensor.bad.data");
                    }
                    length = ((reply.get(7) & 0xFF) << 8) | reply.get(8) & 0xFF;
                }
                if (index >= (length + 8)) {
                    byte[] result = new byte[length - 1];
                    result[0] = reply.get(6);
                    for (int i = 0; i < (length - 2); i++) {
                        result[1 + i] = reply.get(9 + i);
                    }
                    return result;
                }
                index++;
            }
        } catch (IOException ex) {
            throw new FingerprintException("Can't read data from sensor", "sensor.cant.read", ex);
        }
    }

    public void waitForInputAvailable(int index) throws IOException, FingerprintException {
        int timer = 0;
        while (input.available() == 0) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignore) {
            }
            timer++;
            if (timer >= 150) {
                throw new FingerprintException("Timeout at index " + index, "sensor.not.responding");
            }
        }
    }

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }
}
