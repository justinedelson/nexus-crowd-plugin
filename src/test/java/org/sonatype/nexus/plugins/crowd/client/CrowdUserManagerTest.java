/**
 *
 */
package org.sonatype.nexus.plugins.crowd.client;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Set;

import junitx.util.PrivateAccessor;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

import com.atlassian.crowd.integration.SearchContext;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.service.UserManager;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SearchRestriction;
import com.google.common.collect.Sets;

/**
 * @author justin
 *
 */
public class CrowdUserManagerTest {
    private CrowdUserManager locator;
    private CrowdClientHolder holder;
    private UserManager userManager;
    private NexusRoleManager nexusRoleManager;

    @Before
    public void setup() throws Exception {
        holder = createMock(CrowdClientHolder.class);
        userManager = createStrictMock(UserManager.class);
        nexusRoleManager = createStrictMock(NexusRoleManager.class);
        locator = new CrowdUserManager();
        PrivateAccessor.setField(locator, "crowdClientHolder", holder);
    }

    @Test
    public void testSearchUserById() throws Exception {
        Capture<SearchRestriction[]> restrictions = new Capture<SearchRestriction[]>();
        expect(holder.getUserManager()).andReturn(userManager);
        expect(holder.getNexusRoleManager()).andReturn(nexusRoleManager);

        expect(userManager.getUser("user1")).andReturn(new SOAPPrincipal("user1"));
        expect(nexusRoleManager.getNexusRoles("user1")).andReturn(Arrays.asList("one", "two"));

        replay(holder, userManager, nexusRoleManager);

        User u = locator.getUser("user1");
        assertThat(u.getRoles().size(), equalTo(2));

        verify(holder, userManager, nexusRoleManager);
    }

    @Test
    public void testSearchUserByIdViaCriteria() throws Exception {
        Capture<SearchRestriction[]> restrictions = new Capture<SearchRestriction[]>();
        expect(holder.getUserManager()).andReturn(userManager);
        expect(holder.getNexusRoleManager()).andReturn(nexusRoleManager).times(2);

        expect(userManager.searchUsers(capture(restrictions))).andReturn(Arrays.asList(user1, user2));
        expect(nexusRoleManager.getNexusRoles("user1")).andReturn(Arrays.asList("one", "two"));
        expect(nexusRoleManager.getNexusRoles("user2")).andReturn(Arrays.asList("two"));

        replay(holder, userManager, nexusRoleManager);

        UserSearchCriteria critera = new UserSearchCriteria("me");

        Set<User> set = locator.searchUsers(critera);
        assertThat(set.size(), equalTo(2));
        assertThat("wrong restrictions: " + Arrays.asList(restrictions.getValue()), restrictions.getValue().length,
                equalTo(4));
        assertThat(restrictions.getValue()[1].getName(), equalTo(SearchContext.PRINCIPAL_NAME));
        assertThat(restrictions.getValue()[1].getValue(), equalTo("me"));

        verify(holder, userManager, nexusRoleManager);
    }

    @Test
    public void testSearchUserFromGroupsViaCriteria() throws Exception {
        Configuration config = new Configuration();
        config.setUseGroups(true);

        Capture<SearchRestriction[]> restrictions = new Capture<SearchRestriction[]>();
        expect(holder.getUserManager()).andReturn(userManager);
        expect(holder.getNexusRoleManager()).andReturn(nexusRoleManager).times(2);
        expect(holder.getConfiguration()).andReturn(config);

        expect(userManager.searchUsers(capture(restrictions))).andReturn(Arrays.asList(user1, user2));
        expect(nexusRoleManager.getNexusRoles("user1")).andReturn(Arrays.asList("one", "two"));
        expect(nexusRoleManager.getNexusRoles("user2")).andReturn(Arrays.asList("two"));

        replay(holder, userManager, nexusRoleManager);

        UserSearchCriteria critera = new UserSearchCriteria("me");
        critera.setOneOfRoleIds(Sets.newHashSet("role1"));

        Set<User> set = locator.searchUsers(critera);
        assertThat(set.size(), equalTo(2));
        assertThat("wrong restrictions: " + Arrays.asList(restrictions.getValue()), restrictions.getValue().length,
                equalTo(5));
        assertThat(restrictions.getValue()[1].getName(), equalTo(SearchContext.PRINCIPAL_NAME));
        assertThat(restrictions.getValue()[1].getValue(), equalTo("me"));

        assertThat(restrictions.getValue()[4].getName(), equalTo(SearchContext.GROUP_PRINCIPAL_MEMBER));
        assertThat(restrictions.getValue()[4].getValue(), equalTo("role1"));

        verify(holder, userManager, nexusRoleManager);
    }

    private static SOAPPrincipal user1;
    private static SOAPPrincipal user2;
    static {
        user1 = new SOAPPrincipal("user1");
        user1.setAttributes(new SOAPAttribute[] { new SOAPAttribute(CrowdUserManager.ATTRIBUTE_MAIL, "test1@test.com"),
                new SOAPAttribute(CrowdUserManager.ATTRIBUTE_FIRST_NAME, "First1"),
                new SOAPAttribute(CrowdUserManager.ATTRIBUTE_LAST_NAME, "Last1") });

        user2 = new SOAPPrincipal("user2");
        user2.setAttributes(new SOAPAttribute[] { new SOAPAttribute(CrowdUserManager.ATTRIBUTE_MAIL, "test2@test.com"),
                new SOAPAttribute(CrowdUserManager.ATTRIBUTE_FIRST_NAME, "First2"),
                new SOAPAttribute(CrowdUserManager.ATTRIBUTE_LAST_NAME, "Last2") });

    }

    @Test
    public void testGetUser() throws Exception {
        expect(holder.getUserManager()).andReturn(userManager);
        expect(holder.getNexusRoleManager()).andReturn(nexusRoleManager);
        expect(userManager.getUser("user1")).andReturn(user1);
        expect(nexusRoleManager.getNexusRoles("user1")).andReturn(Arrays.asList("one", "two"));
        replay(holder, userManager, nexusRoleManager);

        User u = locator.getUser("user1");
        assertThat(u.getEmailAddress(), equalTo("test1@test.com"));
        assertThat(u.getName(), equalTo("First1 Last1"));
        assertThat(u.getRoles().size(), equalTo(2));

        verify(holder, userManager, nexusRoleManager);
    }

    @Test
    public void testGetUserDoesntExist() throws Exception {
        expect(holder.getUserManager()).andReturn(userManager);
        expect(userManager.getUser("user1")).andThrow(new ObjectNotFoundException());
        replay(holder, userManager);
        User u = locator.getUser("user1");
        assertThat(u, nullValue());

        verify(holder, userManager);
    }
}
