package com.github.shaxbee.uuid;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by shaxbee on 1/3/14.
 */
public class RandomUUIDTest {

    @Test
    public void testUUID4() {
        final UUID uuid = UUID.uuid4();
        assertThat(uuid.getVariant(), equalTo(UUID.Variant.RFC_4122));
        assertThat(uuid.getVersion(), equalTo(4));

        assertThat("Generated UUID is not unique", uuid, not(UUID.uuid4()));
    }
}
