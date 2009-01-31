package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SearchRestriction;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

@Component(role = PlexusUserLocator.class, hint = "Crowd")
public class CrowdUserLocator extends BaseCrowdLocator implements PlexusUserLocator {

    protected Function<SOAPPrincipal, PlexusUser> userConverter = new Function<SOAPPrincipal, PlexusUser>() {

        public PlexusUser apply(SOAPPrincipal from) {
            return convertToPlexusUser(from);
        }
    };

    public String getSource() {
        return SOURCE;
    }

    public PlexusUser getUser(String userId) {
        try {
            SOAPPrincipal principal = getCrowdClient().findPrincipalByName(userId);
            return convertToPlexusUser(principal);
        } catch (RemoteException e) {
            getLogger().error("Unable to look up user " + userId, e);
            return null;
        } catch (InvalidAuthorizationTokenException e) {
            getLogger().error("Unable to look up user " + userId, e);
            return null;
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }

    public boolean isPrimary() {
        return true;
    }

    public Set<String> listUserIds() {
        try {
            String[] names = getCrowdClient().findAllPrincipalNames();
            return new HashSet<String>(Arrays.asList(names));
        } catch (Exception e) {
            getLogger().error("Unable to get username list", e);
            return Collections.emptySet();
        }
    }

    public Set<PlexusUser> listUsers() {
        try {
            SOAPPrincipal[] principals = getCrowdClient().findAllPrincipals();
            return Sets.newHashSet(Iterables.transform(Arrays.asList(principals), userConverter));

        } catch (Exception e) {
            getLogger().error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }

    public Set<PlexusUser> searchUserById(String userId) {
        try {
            SOAPPrincipal[] principals = getCrowdClient().searchPrincipals(
                    new SearchRestriction[] { new SearchRestriction("PRINCIPAL_NAME", userId) });
            return Sets.newHashSet(Iterables.transform(Arrays.asList(principals), userConverter));
        } catch (Exception e) {
            getLogger().error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }

}
