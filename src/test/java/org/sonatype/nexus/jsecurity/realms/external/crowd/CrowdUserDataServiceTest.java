package org.sonatype.nexus.jsecurity.realms.external.crowd;

import static org.easymock.EasyMock.*;

import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Assert;
import org.junit.Test;

import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.mtvi.plateng.testing.ReflectionHelper;

public class CrowdUserDataServiceTest {

    @Test
    public void checkWhenUsingGroups() throws Exception {
        String[] returnedGroups = new String[] { "admin", "developer" };
        SecurityServerClient client = createMock(SecurityServerClient.class);
        expect(client.findGroupMemberships("username")).andReturn(returnedGroups);
        replay(client);

        CrowdUserDataService service = new CrowdUserDataService();
        ReflectionHelper.setField(service, "useGroups", true);
        ReflectionHelper.setField(service, "crowdClient", client);
        service.enableLogging(new ConsoleLogger(Logger.LEVEL_INFO, ""));

        List<String> roles = service.getRoles("username");

        verify(client);
        Assert.assertArrayEquals(returnedGroups, roles.toArray(new String[0]));
    }

    @Test
    public void checkWhenUsingRoles() throws Exception {
        String[] returnedRoles = new String[] { "admin", "developer" };
        SecurityServerClient client = createMock(SecurityServerClient.class);
        expect(client.findRoleMemberships("username")).andReturn(returnedRoles);
        replay(client);

        CrowdUserDataService service = new CrowdUserDataService();
        ReflectionHelper.setField(service, "useGroups", false);
        ReflectionHelper.setField(service, "crowdClient", client);
        service.enableLogging(new ConsoleLogger(Logger.LEVEL_INFO, ""));

        List<String> roles = service.getRoles("username");

        verify(client);
        Assert.assertArrayEquals(returnedRoles, roles.toArray(new String[0]));
    }

}
