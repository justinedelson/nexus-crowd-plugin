/**
 *
 */
package org.sonatype.nexus.plugins.crowd.client;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.RoleMappingUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

import com.atlassian.crowd.integration.SearchContext;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPEntity;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SearchRestriction;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author justin
 *
 */
@Component(role = UserManager.class, hint = "Crowd")
public class CrowdUserManager extends AbstractReadOnlyUserManager implements UserManager, RoleMappingUserManager {

    public CrowdUserManager() {
        logger.info("CrowdUserManager is starting...");
    }

    protected static final String ATTRIBUTE_LAST_NAME = "sn";

    protected static final String ATTRIBUTE_FIRST_NAME = "givenName";

    protected static final String ATTRIBUTE_MAIL = "mail";

    protected static final String REALM_NAME = "Crowd";

    protected static final String SOURCE = "Crowd";

    /**
     * Get a single (the first) value from an attribute. Since Crowd attributes
     * can be polyvalent, but are mostly univalent, there is frequently a lot of
     * unnecessary verbosity.
     *
     * @param entity the entity to access
     * @param attributeName the attribute name
     * @return the first attribute value or null if there are no values or the
     *         attribute doesn't exist
     */
    private static String getAttributeValue(SOAPEntity entity, String attributeName) {
        SOAPAttribute attr = entity.getAttribute(attributeName);
        if (attr != null && attr.getValues().length == 1) {
            return attr.getValues()[0];
        } else {
            return null;
        }
    }

    /**
     * Set a bean property if (and only if) the attribute exists and is
     * non-blank on the provided entity.
     *
     * @param bean the bean on which to set a property
     * @param property the property name to set
     * @param entity the Crowd entity from which to retreive the attribute
     * @param attributeName the attribute name
     * @throws IllegalAccessException if something goes wrong
     * @throws InvocationTargetException if something goes wrong
     * @throws NoSuchMethodException if something goes wrong
     */
    private static void setIfAttributeExists(Object bean, String property, SOAPEntity entity, String attributeName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        SOAPAttribute attr = entity.getAttribute(attributeName);
        if (attr != null && attr.getValues().length == 1) {
            PropertyUtils.setProperty(bean, property, attr.getValues()[0]);
        }
    }

    /**
     * The maximum number of results that will be returned from a user query.
     */
    @Configuration(value = "25")
    private int maxResults;

    @Requirement
    private CrowdClientHolder crowdClientHolder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     */
    public String getAuthenticationRealmName() {
        return REALM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getSource() {
        return SOURCE;
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(String userId) throws UserNotFoundException {
        if (crowdClientHolder.isConfigured()) {
            try {
                SOAPPrincipal principal = crowdClientHolder.getUserManager().getUser(userId);
                return convertToUser(principal);
            } catch (RemoteException e) {
                logger.error("Unable to look up user " + userId, e);
                return null;
            } catch (InvalidAuthorizationTokenException e) {
                logger.error("Unable to look up user " + userId, e);
                return null;
            } catch (ObjectNotFoundException e) {
                return null;
            }
        } else {
            throw new UserNotFoundException("Crowd plugin is not configured.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<RoleIdentifier> getUsersRoles(String userId, String userSource) throws UserNotFoundException {
        if (SOURCE.equals(userSource)) {
            if (crowdClientHolder.isConfigured()) {
                List<String> roleNames = null;
                try {
                    roleNames = crowdClientHolder.getNexusRoleManager().getNexusRoles(userId);
                } catch (RemoteException e) {
                    logger.error("Unable to look up user " + userId, e);
                    return Collections.emptySet();
                } catch (InvalidAuthorizationTokenException e) {
                    logger.error("Unable to look up user " + userId, e);
                    return Collections.emptySet();
                } catch (ObjectNotFoundException e) {
                    throw new UserNotFoundException(userId);
                }
                return Sets.newHashSet(Iterables.transform(roleNames, new Function<String, RoleIdentifier>() {

                    public RoleIdentifier apply(String from) {
                        return new RoleIdentifier(SOURCE, from);
                    }
                }));
            } else {
                throw new UserNotFoundException("Crowd plugin is not configured.");
            }
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( { "unchecked", "deprecation" })
    public Set<String> listUserIds() {
        if (crowdClientHolder.isConfigured()) {
            try {
                List<String> names = crowdClientHolder.getUserManager().getAllUserNames();
                return new HashSet<String>(names);
            } catch (Exception e) {
                logger.error("Unable to get username list", e);
                return Collections.emptySet();
            }
        } else {
            UnconfiguredNotifier.unconfigured();
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<User> listUsers() {
        return searchUsers(new UserSearchCriteria());
    }

    /**
     * {@inheritDoc}
     */
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        return search(criteria.getUserId(), criteria.getOneOfRoleIds(), criteria.getEmail());
    }

    /**
     * {@inheritDoc}
     */
    public void setUsersRoles(String userId, String userSource, Set<RoleIdentifier> roleIdentifiers)
            throws UserNotFoundException, InvalidConfigurationException {
        super.setUsersRoles(userId, roleIdentifiers);
    }

    @SuppressWarnings("unchecked")
    private Set<User> search(String userId, Set<String> roles, String email) {
        if (!crowdClientHolder.isConfigured()) {
            UnconfiguredNotifier.unconfigured();
            return Collections.emptySet();
        }

        List<SearchRestriction> searchRestrictions = new ArrayList<SearchRestriction>();

        searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_ACTIVE, "true"));
        if (StringUtils.isNotEmpty(userId)) {
            searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_NAME, userId));
        } else {
            searchRestrictions.add(new SearchRestriction(SearchContext.PRINCIPAL_NAME, ""));
        }
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_INDEX_START, "0"));
        searchRestrictions.add(new SearchRestriction(SearchContext.SEARCH_MAX_RESULTS, Integer.toString(maxResults)));
        if (roles != null) {
            for (String roleName : roles) {
                if (crowdClientHolder.getConfiguration().isUseGroups()) {
                    searchRestrictions.add(new SearchRestriction(SearchContext.GROUP_PRINCIPAL_MEMBER, roleName));
                } else {
                    searchRestrictions.add(new SearchRestriction(SearchContext.ROLE_PRINCIPAL_MEMBER, roleName));
                }
            }
        }

        try {
            List<SOAPPrincipal> principals = crowdClientHolder.getUserManager().searchUsers(
                    searchRestrictions.toArray(new SearchRestriction[0]));
            return Sets.newHashSet(Iterables.transform(principals, new Function<SOAPPrincipal, User>() {

                public User apply(SOAPPrincipal from) {
                    return convertToUser(from);
                }
            }));
        } catch (Exception e) {
            logger.error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }

    protected User convertToUser(SOAPPrincipal principal) {
        User user = new DefaultUser();
        user.setUserId(principal.getName());
        try {
            setIfAttributeExists(user, "emailAddress", principal, ATTRIBUTE_MAIL);
        } catch (Exception e) {
        }
        String givenName = getAttributeValue(principal, ATTRIBUTE_FIRST_NAME);
        String surName = getAttributeValue(principal, ATTRIBUTE_LAST_NAME);
        user.setName(String.format("%s %s", givenName, surName));
        user.setSource(SOURCE);
        user.setStatus(principal.isActive() ? UserStatus.active : UserStatus.disabled);
        user.setReadOnly(true);
        try {
            user.setRoles(getUsersRoles(principal.getName(), SOURCE));
        } catch (UserNotFoundException e) {
        }

        return user;
    }

}
