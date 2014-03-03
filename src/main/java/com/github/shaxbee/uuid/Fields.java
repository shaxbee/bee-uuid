package com.github.shaxbee.uuid;

import com.github.shaxbee.uuid.UUID.Variant;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 */
class Fields {
    private static final int TIME_LOW = 0;
    private static final int TIME_MID = 4;
    private static final int TIME_HI_AND_VERSION = 6;
    private static final int CLOCK_SEQ_HI_AND_RESERVED = 8;
    private static final int CLOCK_SEQ_LOW = 9;
    private static final int NODE = 10;

    static {
        if (!buffer().hasArray()) {
            throw new IllegalStateException("ByteArray.array() support required.");
        }
    }

    public static int readTimeLow(ByteBuffer source) {
        return source.getInt(TIME_LOW);
    }

    public static short readTimeMid(ByteBuffer source) {
        return source.getShort(TIME_MID);
    }

    public static short readTimeHiAndVersion(ByteBuffer source) {
        return source.getShort(TIME_HI_AND_VERSION);
    }

    public static byte readClockSeqAndReserved(ByteBuffer source) {
        return source.get(CLOCK_SEQ_HI_AND_RESERVED);
    }

    public static byte readClockSeqLow(ByteBuffer source) {
        return source.get(CLOCK_SEQ_LOW);
    }

    public static long readNode(ByteBuffer source) {
        return (source.getLong(NODE) >> 16) & 0xFFFFFFFFFFFFL;
    }

    public static Variant readVariant(ByteBuffer source) {
        final byte clockSeq = source.get(Fields.CLOCK_SEQ_HI_AND_RESERVED);
        // MSB - 0 x x
        if ((clockSeq & 0x80) == Variant.RESERVED_NCS.value()) {
            return Variant.RESERVED_NCS;
            // MSB - 1 0 x
        } else if ((clockSeq & 0xC0) == Variant.RFC_4122.value()) {
            return Variant.RFC_4122;
            // MSB - 1 1 0
        } else if ((clockSeq & 0xE0) == Variant.RESERVED_MICROSOFT.value()) {
            return Variant.RESERVED_MICROSOFT;
            // MSB - 1 1 1
        } else {
            return Variant.RESERVED_FUTURE;
        }
    }

    public static int readVersion(ByteBuffer source) {
        return ((source.getShort(TIME_HI_AND_VERSION) & 0xFF00) >> 12) & 0x000F;
    }

    public static void writeVersion(ByteBuffer output, int version) {
        final byte clockSeq = (byte) ((output.get(CLOCK_SEQ_HI_AND_RESERVED) & ~0xC0) | Variant.RFC_4122.value());
        output.put(CLOCK_SEQ_HI_AND_RESERVED, clockSeq);

        final short timeHi = (short) ((output.getShort(TIME_HI_AND_VERSION) & ~0xF000) | version << 12);
        output.putShort(TIME_HI_AND_VERSION, timeHi);
    }

    public static ByteBuffer buffer() {
        return ByteBuffer.allocate(18).order(ByteOrder.BIG_ENDIAN);
    }
}
