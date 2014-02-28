package com.github.shaxbee.uuid;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import java.text.ParseException;

import org.junit.Test;

import com.github.shaxbee.uuid.UUID.Namespace;
import com.github.shaxbee.uuid.UUID.Variant;

public class NameBasedUUIDTest {
	
	@Test
	public void testUUID3() throws ParseException {
		final UUID uuid = UUID.uuid3(Namespace.DNS, "github.com/shaxbee".getBytes());
		assertThat(uuid.getVariant(), equalTo(Variant.RFC_4122));
		assertThat(uuid.getVersion(), equalTo(3));
		assertThat(uuid.hex(), equalTo("ef3b9bbb-33c9-38c3-adf4-92ca39bea40a"));
	}
	
	@Test
	public void testUUID5() {
		final UUID uuid = UUID.uuid5(Namespace.DNS, "github.com/shaxbee".getBytes());
		assertThat(uuid.getVariant(), equalTo(Variant.RFC_4122));
		assertThat(uuid.getVersion(), equalTo(5));
		assertThat(uuid.hex(), equalTo("0a7c0d44-9325-5a00-a0ed-67815c8df4b0"));
	}

}
