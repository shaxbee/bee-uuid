package com.github.shaxbee.uuid;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;

public class UUID {

    private final ByteBuffer bytes_;
    private static final SecureRandom secureRandom_ = makeSecureRandom();

    public enum Variant {
		RESERVED_NCS(0x00), RFC_4122(0x80), RESERVED_MICROSOFT(0xC0), RESERVED_FUTURE(0xE0);

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

    public static UUID uuid3(final Namespace namespace, final byte[] name) {
        return new UUID(digest("MD5", 3, namespace, name));
    }

    public static UUID uuid5(final Namespace namespace, final byte[] name) {
        return new UUID(digest("SHA-1", 5, namespace, name));
    }

    public static UUID uuid4() {
        ByteBuffer result = Fields.buffer();
        secureRandom_.nextBytes(result.array());
        Fields.writeVersion(result, 4);

        return new UUID(result);
    }

	public static UUID fromBytes(final byte[] bytes) {
		if(bytes == null) {
			throw new NullPointerException();
		}
		
		if(bytes.length != 16) {
			throw new IllegalArgumentException("Invalid UUID length");
		}
		
		return new UUID(Parser.validate(Fields.buffer().put(bytes)));
	}
	
	public static UUID fromString(final String source) throws ParseException {
		return new UUID(Parser.parse(source));
	}
	
	public static UUID fromFields(final int timeLow, final short timeMid, final short timeHiAndVersion, final byte clockSeqAndReserved, final byte clockSeqLow, final long node) {
        ByteBuffer result = Fields.buffer()
                .putInt(timeLow)
                .putShort(timeMid)
                .putShort(timeHiAndVersion)
                .put(clockSeqAndReserved)
                .put(clockSeqLow)
                .putLong(node << 16);

		return new UUID(Parser.validate(result));
	}

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof UUID)) {
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
		return String.format("%08x-%04x-%04x-%02x%02x-%012x", getTimeLow(), getTimeMid(),
				getTimeHiAndVersion(), getClockSeqAndReserved(),
				getClockSeqLow(), getNode());
	}

	public String urn() {
		return String.format("urn:uuid:%08x-%04x-%04x-%02x%02x-%012x", getTimeLow(), getTimeMid(),
				getTimeHiAndVersion(), getClockSeqAndReserved(),
				getClockSeqLow(), getNode());
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

	private static ByteBuffer digest(final String algorithm, final int version,
			final Namespace namespace, final byte[] name) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);

			digest.update(namespace.bytes().array(), 0, 16);
			digest.update(name);

            final ByteBuffer result = Fields.buffer();
            result.put(digest.digest(), 0, 16);
			Fields.writeVersion(result, version);

			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

    private static SecureRandom makeSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch(NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalStateException(e);
        }
    }
}
