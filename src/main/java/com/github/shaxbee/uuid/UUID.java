package com.github.shaxbee.uuid;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;

public class UUID {

    private final ByteBuffer bytes_;

    private static final SecureRandom SECURE_RANDOM;
    private static final MessageDigest MD5_DIGEST;
    private static final MessageDigest SHA1_DIGEST;

    public enum Variant {
        RESERVED_NCS(0x00), RFC_4122(0x80), RESERVED_MICROSOFT(0xC0), RESERVED_FUTURE(
                        0xE0);

        private int value_;

        private Variant(int value) {
            value_ = value;
        }

        public int value() {
            return value_;
        }
    }

    public enum Namespace {
        DNS("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
        URL("6ba7b811-9dad-11d1-80b4-00c04fd430c8"),
        OID("6ba7b812-9dad-11d1-80b4-00c04fd430c8"),
        X500("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

        private final ByteBuffer bytes_;

        private Namespace(String uuid) {
            try {
                bytes_ = Parser.parse(uuid);
            } catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }

        private ByteBuffer bytes() {
            return bytes_;
        }
    }

    static {
        // verify that required algorithms are available in environment
        try {
            SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG");
            MD5_DIGEST = MessageDigest.getInstance("MD5");
            SHA1_DIGEST = MessageDigest.getInstance("SHA-1");

            MD5_DIGEST.clone();
            SHA1_DIGEST.clone();
        } catch (NoSuchAlgorithmException | CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate Name-based UUIDv3 using MD5 digest.
     * 
     * Be aware that two uuid3() calls with same argument will yield same UUID.
     * Therefore uniqueness is guaranteed as long as name argument is unique.
     * 
     * @param namespace
     *            RFC4122 namespace
     * @param name
     *            Unique identifier
     */
    public static UUID uuid3(final Namespace namespace, final byte[] name) {
        if (namespace == null || name == null) {
            throw new NullPointerException();
        }
        ;

        return new UUID(digest(MD5_DIGEST, 3, namespace, name));
    }

    /**
     * Generate Name-based UUIDv5 using SHA-1 digest.
     * 
     * Be aware that two uuid5() calls with same argument will yield same UUID.
     * Therefore uniqueness is guaranteed as long as name argument is unique.
     * 
     * @param namespace
     *            RFC4122 namespace
     * @param name
     *            Unique identifier
     */
    public static UUID uuid5(final Namespace namespace, final byte[] name) {
        if (namespace == null || name == null) {
            throw new NullPointerException();
        }

        return new UUID(digest(SHA1_DIGEST, 5, namespace, name));
    }

    /**
     * Generate random UUIDv4.
     */
    public static UUID uuid4() {
        ByteBuffer result = Fields.buffer();
        SECURE_RANDOM.nextBytes(result.array());
        Fields.writeVersion(result, 4);

        return new UUID(result);
    }

    /**
     * Construct UUID from raw bytes.
     * 
     * Validation is done for RFC4122 UUID only.
     * 
     * @param bytes
     *            Raw 16-byte long UUID
     * 
     * @throws IllegalArgumentException
     *             UUID has invalid length
     * @throws ParseException
     *             One or more fields are invalid
     */
    public static UUID fromBytes(final byte[] bytes) throws ParseException {
        if (bytes == null) {
            throw new NullPointerException();
        }

        if (bytes.length != 16) {
            throw new IllegalArgumentException("Invalid UUID length");
        }

        return new UUID(Parser.validate(Fields.buffer().put(bytes)));
    }

    /**
     * Parse UUID from string.
     * 
     * @param source
     *            Hex, hyphen-delimited UUID string
     * 
     * @throws ParseException
     *             Invalid format of UUID
     */
    public static UUID fromString(final String source) throws ParseException {
        if (source == null) {
            throw new NullPointerException();
        }

        return new UUID(Parser.parse(source));
    }

    public static UUID fromFields(final int timeLow, final short timeMid,
                    final short timeHiAndVersion, final byte clockSeqAndReserved,
                    final byte clockSeqLow, final long node)
                    throws IllegalArgumentException {
        ByteBuffer result = Fields.buffer().putInt(timeLow).putShort(timeMid)
                        .putShort(timeHiAndVersion).put(clockSeqAndReserved)
                        .put(clockSeqLow).putLong(node << 16);

        try {
            return new UUID(Parser.validate(result));
        } catch (final ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UUID)) {
            return false;
        }

        return Arrays.equals(bytes_.array(), ((UUID) other).bytes_.array());
    }

    @Override
    public String toString() {
        return String.format("UUID(\"%s\")", hex());
    }

    private UUID(ByteBuffer bytes) {
        bytes_ = bytes;
    }

    public Variant getVariant() {
        return Fields.readVariant(bytes_);
    }

    public int getVersion() {
        return Fields.readVersion(bytes_);
    }

    public byte[] bytes() {
        return Arrays.copyOf(bytes_.array(), 16);
    }

    public String hex() {
        return String.format("%08x-%04x-%04x-%02x%02x-%012x", getTimeLow(),
                        getTimeMid(), getTimeHiAndVersion(), getClockSeqAndReserved(),
                        getClockSeqLow(), getNode());
    }

    public String urn() {
        return String.format("urn:uuid:%08x-%04x-%04x-%02x%02x-%012x",
                        getTimeLow(), getTimeMid(), getTimeHiAndVersion(),
                        getClockSeqAndReserved(), getClockSeqLow(), getNode());
    }

    public int getTimeLow() {
        return Fields.readTimeLow(bytes_);
    }

    public short getTimeMid() {
        return Fields.readTimeMid(bytes_);
    }

    public short getTimeHiAndVersion() {
        return Fields.readTimeHiAndVersion(bytes_);
    }

    public byte getClockSeqAndReserved() {
        return Fields.readClockSeqAndReserved(bytes_);
    }

    public byte getClockSeqLow() {
        return Fields.readClockSeqLow(bytes_);
    }

    public long getNode() {
        return Fields.readNode(bytes_);
    }

    private static ByteBuffer digest(final MessageDigest algorithm, final int version, final Namespace namespace,
                    final byte[] name) {
        try {
            // clone digest for thread safety
            final MessageDigest digest = (MessageDigest) algorithm.clone();

            digest.update(namespace.bytes().array(), 0, 16);
            digest.update(name);

            final ByteBuffer result = Fields.buffer();
            result.put(digest.digest(), 0, 16);
            Fields.writeVersion(result, version);

            return result;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
