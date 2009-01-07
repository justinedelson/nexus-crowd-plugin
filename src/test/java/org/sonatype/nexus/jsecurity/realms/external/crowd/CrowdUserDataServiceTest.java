/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.jsecurity.realms.external.crowd;

import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.Test;

import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.mtvi.plateng.testing.ReflectionHelper;

public class CrowdUserDataServiceTest {

    @Test
    public void checkAnonymousAutc() throws Exception {
        CrowdUserDataService service = new CrowdUserDataService();
        UsernamePasswordToken token = new UsernamePasswordToken("anonymous", "anonymous");
        SimpleAuthenticationInfo authInfo = (SimpleAuthenticationInfo) service.authenticate(token,
                "test");
        Assert.assertEquals("anonymous", authInfo.getPrincipals().oneByType(String.class));
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void checkBadAnonymousAutc() throws Exception {
        SecurityServerClient client = createMock(SecurityServerClient.class);
        expect(client.authenticatePrincipalSimple("anonymous", "anonymous1")).andThrow(
                new InvalidAuthenticationException());
        replay(client);

        CrowdUserDataService service = new CrowdUserDataService();
        ReflectionHelper.setField(service, "crowdClient", client);

        UsernamePasswordToken token = new UsernamePasswordToken("anonymous", "anonymous1");
        service.authenticate(token, "test");
    }

    @Test
    public void checkAnonymousAutz() throws Exception {
        CrowdUserDataService service = new CrowdUserDataService();
        List<String> roles = service.getRoles("anonymous");
        Assert.assertEquals(Arrays.asList("anonymous"), roles);

        ReflectionHelper.setField(service, "anonymousRole", "custom-anonymous");
        roles = service.getRoles("anonymous");
        Assert.assertEquals(Arrays.asList("custom-anonymous"), roles);
    }

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
