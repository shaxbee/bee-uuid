package com.github.shaxbee.uuid;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.junit.Test;

import com.github.shaxbee.uuid.UUID.Namespace;
import com.github.shaxbee.uuid.UUID.Variant;

public class NameBasedUUIDTest {

    private static final byte[] NAME;

    static {
	try {
	    NAME = "github.com/shaxbee".getBytes("UTF-8");
	} catch (UnsupportedEncodingException e) {
	    throw new IllegalStateException(e);
	}
    }

    @Test
    public void testUUID3() throws ParseException {
	final UUID uuid = UUID.uuid3(Namespace.DNS, NAME);
	assertThat(uuid.getVariant(), equalTo(Variant.RFC_4122));
	assertThat(uuid.getVersion(), equalTo(3));
	assertThat(uuid.hex(), equalTo("ef3b9bbb-33c9-38c3-adf4-92ca39bea40a"));
	assertThat(uuid.urn(), equalTo("urn:uuid:ef3b9bbb-33c9-38c3-adf4-92ca39bea40a"));

	assertThat(UUID.uuid3(Namespace.OID, NAME).hex(), equalTo("e039ff9d-be36-390e-a3a1-23a2969c09f9"));
	assertThat(UUID.uuid3(Namespace.URL, NAME).hex(), equalTo("79fa69d5-9b6a-3158-a6a0-ee467d1eb526"));
	assertThat(UUID.uuid3(Namespace.X500, NAME).hex(), equalTo("98b6a7fb-8d2d-3675-87fd-a220208cb578"));
    }

    @Test
    public void testUUID5() {
	final UUID uuid = UUID.uuid5(Namespace.DNS, NAME);
	assertThat(uuid.getVariant(), equalTo(Variant.RFC_4122));
	assertThat(uuid.getVersion(), equalTo(5));
	assertThat(uuid.hex(), equalTo("0a7c0d44-9325-5a00-a0ed-67815c8df4b0"));
	assertThat(uuid.urn(), equalTo("urn:uuid:0a7c0d44-9325-5a00-a0ed-67815c8df4b0"));

	assertThat(UUID.uuid5(Namespace.OID, NAME).hex(), equalTo("2a84fda5-ca07-5725-af49-8ea98153fac3"));
	assertThat(UUID.uuid5(Namespace.URL, NAME).hex(), equalTo("575458d3-8483-5097-9575-0b9c442780a1"));
	assertThat(UUID.uuid5(Namespace.X500, NAME).hex(), equalTo("e5af7352-4831-5545-9399-0d1b1a6d8ff4"));
    }

}
