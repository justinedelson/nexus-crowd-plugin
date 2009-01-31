package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.nexus.jsecurity.realms.external.crowd.CrowdAuthenticatingRealm;

import com.atlassian.crowd.integration.SearchContext;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SearchRestriction;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Implementation of PlexusUserLocator that is backed by an instance of
 * Atlassian Crowd.
 * 
 * @author Justin Edelson
 * 
 */
@Component(role = PlexusUserLocator.class, hint = "Crowd")
public class CrowdUserLocator extends BaseCrowdLocator implements PlexusUserLocator {

    /**
     * The maximum number of results that will be returned from a user query.
     */
    @Configuration(value = "25")
    private int maxResults;

    /**
     * {@inheritDoc}
     */
    public String getSource() {
        return SOURCE;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean isPrimary() {
        return CrowdAuthenticatingRealm.isActive();
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> listUserIds() {
        try {
            String[] names = getCrowdClient().findAllPrincipalNames();
            return new HashSet<String>(Arrays.asList(names));
        } catch (Exception e) {
            getLogger().error("Unable to get username list", e);
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public Set<PlexusUser> listUsers() {
        return search("");
    }

    /**
     * {@inheritDoc}
     */
    public Set<PlexusUser> searchUserById(String userId) {
        return search(userId);
    }

    @SuppressWarnings("unchecked")
    private Set<PlexusUser> search(String userId) {
        List<SearchRestriction> searchRestrictions = new ArrayList<SearchRestriction>();

        searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_ACTIVE, "true"));
        searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_NAME, userId));
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_INDEX_START, "1"));
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_MAX_RESULTS, Integer
                .toString(maxResults)));

        try {
            SOAPPrincipal[] principals = getCrowdClient().searchPrincipals(
                    searchRestrictions.toArray(new SearchRestriction[0]));
            return Sets.newHashSet(Iterables.transform(Arrays.asList(principals),
                    new Function<SOAPPrincipal, PlexusUser>() {

                        public PlexusUser apply(SOAPPrincipal from) {
                            return convertToPlexusUser(from);
                        }
                    }));
        } catch (Exception e) {
            getLogger().error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }

}
