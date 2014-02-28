package com.github.shaxbee.uuid;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;

public class UUID {

	public enum Variant {
		RESERVED_NCS, RFC_4122, RESERVED_MICROSOFT, RESERVED_FUTURE
	}

	public enum Namespace {
		DNS("6ba7b810-9dad-11d1-80b4-00c04fd430c8"), 
		URL("6ba7b811-9dad-11d1-80b4-00c04fd430c8"), 
		OID("6ba7b812-9dad-11d1-80b4-00c04fd430c8"), 
		X500("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

		private final byte[] bytes_;

		private Namespace(String uuid) {
			try {
				bytes_ = UUID.bytes(uuid);
			} catch (ParseException e) {
				throw new IllegalStateException(e);
			}
		}

		private byte[] bytes() {
			return bytes_;
		}
	}
	
	public static UUID fromBytes(final byte[] bytes) {
		if(bytes == null) {
			throw new NullPointerException();
		}
		
		if(bytes.length < 16) {
			throw new IllegalArgumentException("Invalid length of input UUID");
		}
		
		return new UUID(validate(bytes));
	}
	
	public static UUID fromString(final String source) throws ParseException {
		return new UUID(validate(parse(source)));
	}
	
	public static UUID fromFields(int timeLow, int timeMid, int timeHiAndVersion, int clockSeqAndReserved, int clockSeqLow, long node) {
		final byte[] bytes = new byte[16];
		writeField(bytes, Field.TIME_LOW, timeLow);
		writeField(bytes, Field.TIME_MID, timeMid);
		writeField(bytes, Field.TIME_HI_AND_VERSION, timeHiAndVersion);
		writeField(bytes, Field.CLOCK_SEQ_AND_RESERVED, clockSeqAndReserved);
		writeField(bytes, Field.CLOCK_SEQ_LOW, clockSeqLow);
		writeField(bytes, Field.NODE, node);
		
		return new UUID(validate(bytes));
	}
	
	@Override
	public String toString() {
		return String.format("UUID(\"%s\")", hex());
	}

	private UUID(byte[] bytes) {
		bytes_ = bytes;
	}
	
	public Variant getVariant() {
		return extractVariant(bytes_);

	}

	public int getVersion() {
		return extractVersion(bytes_);
	}

	public byte[] bytes() {
		return null;
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
		return (int) readField(Field.TIME_LOW);
	}

	public short getTimeMid() {
		return (short) readField(Field.TIME_MID);
	}

	public short getTimeHiAndVersion() {
		return (short) readField(Field.TIME_HI_AND_VERSION);
	}

	public byte getClockSeqAndReserved() {
		return bytes_[Field.CLOCK_SEQ_AND_RESERVED.offset()];
	}

	public byte getClockSeqLow() {
		return bytes_[Field.CLOCK_SEQ_LOW.offset()];
	}

	public long getNode() {
		 return readField(Field.NODE);
	}

	public static UUID uuid3(final Namespace namespace, final byte[] name) {
		return new UUID(digest("MD5", 3, namespace, name));
	}

	public static UUID uuid5(final Namespace namespace, final byte[] name) {
		return new UUID(digest("SHA-1", 5, namespace, name));
	}

	public static byte[] bytes(final String uuid) throws ParseException {
		return validate(parse(uuid));
	}

	private final byte[] bytes_;
	private static final Pattern RFC4122_PATTERN = Pattern.compile("(\\p{XDigit}{8})-(\\p{XDigit}{4})-(\\p{XDigit}{4})-(\\p{XDigit}{2})(\\p{XDigit}{2})-(\\p{XDigit}{12})");

	private enum Field {
		TIME_LOW(0, 4), TIME_MID(4, 2), TIME_HI_AND_VERSION(6, 2), CLOCK_SEQ_AND_RESERVED(
				8, 1), CLOCK_SEQ_LOW(9, 1), NODE(10, 6);

		private int offset_;
		private int length_;

		private Field(int offset, int length) {
			offset_ = offset;
			length_ = length;
		}

		public int offset() {
			return offset_;
		}

		public int length() {
			return length_;
		}
	};

	private long readField(final Field field) {
		long result = 0;
		for (int index = field.offset(); index < field.offset() + field.length(); index++) {
			result = (result << 8) | (bytes_[index] & 0xFF);
		}
		return result;
	}
	
	private static void writeField(final byte[] bytes, final Field field, final long value) {
		for (int index = field.offset(); index < field.offset() + field.length(); index++) {
			bytes[index] = (byte) ((value >> ((field.length() - index) * 8)) & 0xFF);
		}
	}

	private static byte[] digest(final String algorithm, final int version,
			final Namespace namespace, final byte[] name) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);

			digest.update(namespace.bytes());
			digest.update(name);

			final byte[] result = Arrays.copyOf(digest.digest(), 16);

			// set variant to RFC-4122
			result[Field.CLOCK_SEQ_AND_RESERVED.offset()] &= ~0xC0;
			result[Field.CLOCK_SEQ_AND_RESERVED.offset()] |= 0x80;

			// set version
			result[Field.TIME_HI_AND_VERSION.offset()] &= ~0xF0;
			result[Field.TIME_HI_AND_VERSION.offset()] |= version << 4;

			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static byte[] parse(final String source) throws ParseException {
		if(source == null) {
			throw new NullPointerException();
		}
		
		final Matcher matcher = RFC4122_PATTERN.matcher(source);
		
		if(!matcher.matches()) {
			throw new ParseException("UUID does not conform to RFC4122", 0);
		}
		
		final ByteBuffer output = ByteBuffer.allocate(16);
		
		for(int group = 1; group <= matcher.groupCount(); group++) {
			try {
				output.put(Hex.decodeHex(matcher.group(group).toCharArray()));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
		return output.array();
	}
	
	private static Variant extractVariant(final byte[] bytes) {
		final int source = bytes[Field.CLOCK_SEQ_AND_RESERVED.offset()];
		// MSB - 0 x x
		if ((source & 0x80) == 0) {
			return Variant.RESERVED_NCS;
			// MSB - 1 0 x
		} else if ((source & 0xC0) == 0x80) {
			return Variant.RFC_4122;
			// MSB - 1 1 0
		} else if ((source & 0xE0) == 0xC0) {
			return Variant.RESERVED_MICROSOFT;
			// MSB - 1 1 1
		} else {
			return Variant.RESERVED_FUTURE;
		}		
	}
	
	private static int extractVersion(final byte[] bytes) {
		return (bytes[Field.TIME_HI_AND_VERSION.offset()] & 0xF0) >> 4;		
	}
	
	private static byte[] validate(final byte[] bytes) {
		Variant variant = extractVariant(bytes);
		int version = extractVersion(bytes);
		
		// check if UUID version
		if(variant.equals(Variant.RFC_4122) && (version < 1 || version > 5)) {
			throw new IllegalArgumentException(String.format("Invalid RFC4122 UUID version %d", version));
		}
		
		return bytes;
	}
}
