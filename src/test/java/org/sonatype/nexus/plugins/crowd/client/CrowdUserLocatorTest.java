package org.sonatype.nexus.plugins.crowd.client;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.jsecurity.locators.users.PlexusUser;

import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.mtvi.plateng.testing.ReflectionHelper;

public class CrowdUserLocatorTest {

    private CrowdUserLocator locator;
    private CrowdClient client;

    @Before
    public void setup() throws Exception {
        client = createStrictMock(CrowdClient.class);
        locator = new CrowdUserLocator();
        ReflectionHelper.setField(locator, "crowdClient", client);
    }

    @Test
    public void testGetUser() throws Exception {
        SOAPPrincipal p = new SOAPPrincipal("user1");
        p.setAttributes(new SOAPAttribute[] {
                new SOAPAttribute(BaseCrowdLocator.ATTRIBUTE_MAIL, "test@test.com"),
                new SOAPAttribute(BaseCrowdLocator.ATTRIBUTE_FIRST_NAME, "First"),
                new SOAPAttribute(BaseCrowdLocator.ATTRIBUTE_LAST_NAME, "Last") });
        expect(client.findPrincipalByName("user1")).andReturn(p);
        expect(client.getNexusRoles("user1")).andReturn(Arrays.asList("one", "two"));
        replay(client);

        PlexusUser u = locator.getUser("user1");
        assertThat(u.getEmailAddress(), equalTo("test@test.com"));
        assertThat(u.getName(), equalTo("First Last"));
        assertThat(u.getRoles().size(), equalTo(2));

        verify(client);
    }

    @Test
    public void testGetUserDoesntExist() throws Exception {
        expect(client.findPrincipalByName("user1")).andThrow(new ObjectNotFoundException());
        replay(client);

        PlexusUser u = locator.getUser("user1");
        assertThat(u, nullValue());

        verify(client);
    }

}
