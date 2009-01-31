package org.sonatype.nexus.jsecurity.realms.external.crowd;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrowdAuthenticatingRealmTest {

    private CrowdAuthenticatingRealm realm;

    @Before
    public void setup() {
        realm = new CrowdAuthenticatingRealm();

    }

    @Test
    public void checkActiveFlag() throws Exception {
        assertFalse(realm.isActive());
        realm.initialize();
        assertTrue(realm.isActive());
    }

    @After
    public void teardown() {
        realm.dispose();
    }

}
