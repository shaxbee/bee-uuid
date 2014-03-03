package com.github.shaxbee.uuid;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import org.junit.Test;
import com.github.shaxbee.uuid.UUID.Variant;

public class ParserTest {
	
	@Test(expected=ParseException.class)
	public void testInvalidUUIDFormat() throws ParseException {
		// attempt to parse UUID with non-hex character g at position 1
		Parser.parse("bg209999-0c6c-11d2-97cf-00c04f8eea45");
	}
	
	@Test(expected=ParseException.class)
	public void testInvalidUUIDVersion() throws ParseException {
		// attempt to parse UUID with invalid version value 6
		Parser.parse("ef3b9bbb-33c9-68f3-adf4-92ca39bea40a");
	}
	
	@Test
	public void testMicrosoftUUID() throws ParseException {
		UUID uuid = UUID.fromString("ba209999-0c6c-11d2-d7cf-00c04f8eea45");
		assertThat(uuid.getVariant(), equalTo(Variant.RESERVED_MICROSOFT));
	}
}