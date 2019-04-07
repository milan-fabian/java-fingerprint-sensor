package sk.mimac.fingerprint.adafruit;

/**
 * For fingerprint sensor ZFM-20 / R307. More info at
 * https://github.com/adafruit/Adafruit-Fingerprint-Sensor-Library/.
 *
 * @author Milan Fabian
 */
public class AdafruitConstants {

    public static final int FINGERPRINT_OK = 0x00;
    public static final int FINGERPRINT_PACKETRECIEVEERR = 0x01;
    public static final int FINGERPRINT_NOFINGER = 0x02;
    public static final int FINGERPRINT_IMAGEFAIL = 0x03;
    public static final int FINGERPRINT_IMAGEMESS = 0x06;
    public static final int FINGERPRINT_FEATUREFAIL = 0x07;
    public static final int FINGERPRINT_NOMATCH = 0x08;
    public static final int FINGERPRINT_NOTFOUND = 0x09;
    public static final int FINGERPRINT_ENROLLMISMATCH = 0x0A;
    public static final int FINGERPRINT_BADLOCATION = 0x0B;
    public static final int FINGERPRINT_DBRANGEFAIL = 0x0C;
    public static final int FINGERPRINT_UPLOADFEATUREFAIL = 0x0D;
    public static final int FINGERPRINT_PACKETRESPONSEFAIL = 0x0E;
    public static final int FINGERPRINT_UPLOADFAIL = 0x0F;
    public static final int FINGERPRINT_DELETEFAIL = 0x10;
    public static final int FINGERPRINT_DBCLEARFAIL = 0x11;
    public static final int FINGERPRINT_PASSFAIL = 0x13;
    public static final int FINGERPRINT_INVALIDIMAGE = 0x15;
    public static final int FINGERPRINT_FLASHERR = 0x18;
    public static final int FINGERPRINT_INVALIDREG = 0x1A;
    public static final int FINGERPRINT_ADDRCODE = 0x20;
    public static final int FINGERPRINT_PASSVERIFY = 0x21;

    public static final int FINGERPRINT_STARTCODE = 0xEF01;

    public static final byte FINGERPRINT_COMMANDPACKET = 0x01;
    public static final byte FINGERPRINT_DATAPACKET = 0x02;
    public static final byte FINGERPRINT_ACKPACKET = 0x07;
    public static final byte FINGERPRINT_ENDDATAPACKET = 0x08;

    public static final int FINGERPRINT_TIMEOUT = 0xFF;
    public static final int FINGERPRINT_BADPACKET = 0xFE;

    public static final int FINGERPRINT_GETIMAGE = 0x01;
    public static final int FINGERPRINT_IMAGE2TZ = 0x02;
    public static final int FINGERPRINT_HISPEEDSEARCH = 0x04;
    public static final int FINGERPRINT_REGMODEL = 0x05;
    public static final int FINGERPRINT_STORE = 0x06;
    public static final int FINGERPRINT_LOAD = 0x07;
    public static final int FINGERPRINT_UPLOAD = 0x08;
    public static final int FINGERPRINT_DOWNLOAD = 0x09;
    public static final int FINGERPRINT_DELETE = 0x0C;
    public static final int FINGERPRINT_EMPTY = 0x0D;
    public static final int FINGERPRINT_SET_SYS_PARAM = 0x0E;
    public static final int FINGERPRINT_READ_SYS_PARAM = 0x0F;
    public static final int FINGERPRINT_VERIFYPASSWORD = 0x13;
    public static final int FINGERPRINT_READ_SYS_INFO = 0x16;
    public static final int FINGERPRINT_TEMPLATECOUNT = 0x1D;
}
