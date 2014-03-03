package com.github.shaxbee.uuid;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.shaxbee.uuid.UUID.Variant;

/**
 * Created by shaxbee on 1/3/14.
 */
class Parser {
    private static final Pattern UUID_PATTERN = 
        Pattern.compile("(\\p{XDigit}{8})-(\\p{XDigit}{4})-(\\p{XDigit}{4})-(\\p{XDigit}{2})(\\p{XDigit}{2})-(\\p{XDigit}{12})");

    public static ByteBuffer parse(final String source) throws ParseException {
        if (source == null) {
            throw new NullPointerException();
        }

        final Matcher matcher = UUID_PATTERN.matcher(source);

        if (!matcher.matches()) {
            throw new ParseException("UUID does not conform to RFC4122", 0);
        }

        ByteBuffer output = Fields.buffer();
        for (int group = 1; group <= matcher.groupCount(); group++) {
            decodeHex(output, matcher.group(group));
        }

        return validate(output);
    }

    public static ByteBuffer validate(final ByteBuffer bytes) throws ParseException {
        Variant variant = Fields.readVariant(bytes);
        int version = Fields.readVersion(bytes);

        // check if UUID version
        if (variant.equals(Variant.RFC_4122) && (version < 1 || version > 5)) {
            throw new ParseException(String.format("Invalid RFC4122 UUID version %d", version), 0);
        }

        return bytes;
    }

    public static void decodeHex(final ByteBuffer output, final String source) {
        if (source.length() % 2 != 0) {
            throw new IllegalArgumentException(String.format("Odd string length", source.length()));
        }

        for (int octet = 0; octet < source.length() / 2; octet++) {
            output.put((byte) (
                decodeHexChar(source.charAt(octet * 2)) << 4 | 
                decodeHexChar(source.charAt(octet * 2 + 1))));
        }
    }

    private static int decodeHexChar(final char ch) {
        int result = Character.digit(ch, 16);

        if (result == -1) {
            throw new IllegalArgumentException(String.format("Invalid non-hex character <%s>", ch));
        }

        return result;
    }
}
