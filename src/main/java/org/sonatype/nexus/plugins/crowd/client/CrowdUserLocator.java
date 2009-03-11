/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.nexus.jsecurity.realms.external.crowd.CrowdAuthenticatingRealm;

import com.atlassian.crowd.integration.SearchContext;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SearchRestriction;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

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
            SOAPPrincipal principal = getCrowdClientHolder().getUserManager().getUser(userId);
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
            List<String> names = getCrowdClientHolder().getUserManager().getAllUserNames();
            return new HashSet<String>(names);
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
        return search("", null);
    }

    /**
     * {@inheritDoc}
     */
    public Set<PlexusUser> searchUserById(String userId) {
        return search(userId, null);
    }

    @SuppressWarnings("unchecked")
    private Set<PlexusUser> search(String userId, Set<String> roles) {
        List<SearchRestriction> searchRestrictions = new ArrayList<SearchRestriction>();

        searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_ACTIVE, "true"));
        if (StringUtils.isNotEmpty(userId)) {
            searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_NAME, userId));
        } else {
            searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_NAME, ""));
        }
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_INDEX_START, "1"));
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_MAX_RESULTS, Integer
                .toString(maxResults)));
        if (roles != null) {
            for (String roleName : roles) {
                if (getCrowdClientHolder().getConfiguration().isUseGroups()) {
                    searchRestrictions.add(new SearchRestriction(
                            SearchContext.GROUP_PRINCIPAL_MEMBER, roleName));
                } else {
                    searchRestrictions.add(new SearchRestriction(
                            SearchContext.ROLE_PRINCIPAL_MEMBER, roleName));
                }
            }
        }

        try {
            List<SOAPPrincipal> principals = getCrowdClientHolder().getUserManager().searchUsers(
                    searchRestrictions.toArray(new SearchRestriction[0]));
            return Sets.newHashSet(Iterables.transform(principals,
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

    public Set<PlexusUser> searchUsers(PlexusUserSearchCriteria criteria) {
        return search(criteria.getUserId(), criteria.getOneOfRoleIds());
    }

}
