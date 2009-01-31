package org.sonatype.nexus.plugins.crowd.client;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPEntity;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class BaseCrowdLocator extends AbstractLogEnabled {

    public static final String SOURCE = "Crowd";

    @Requirement
    private CrowdClient crowdClient;

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
    private static void setIfAttributeExists(Object bean, String property, SOAPEntity entity,
            String attributeName) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        SOAPAttribute attr = entity.getAttribute(attributeName);
        if (attr != null && attr.getValues().length == 1) {
            PropertyUtils.setProperty(bean, property, attr.getValues()[0]);
        }
    }

    protected Set<PlexusRole> convertToPlexusRoles(List<String> roleNames) {
        return Sets.newHashSet(Iterables.transform(roleNames, new Function<String, PlexusRole>() {

            public PlexusRole apply(String from) {
                return new PlexusRole(from, from, SOURCE);
            }
        }));
    }

    protected PlexusUser convertToPlexusUser(SOAPPrincipal principal) {
        PlexusUser user = new PlexusUser();
        user.setUserId(principal.getName());
        try {
            setIfAttributeExists(user, "emailAddress", principal, "mail");
        } catch (Exception e) {
        }
        String givenName = getAttributeValue(principal, "givenName");
        String surName = getAttributeValue(principal, "sn");
        user.setName(String.format("%s %s", givenName, surName));
        user.setSource(SOURCE);
        user.setRoles(getRoles(user.getUserId()));

        return user;
    }

    protected CrowdClient getCrowdClient() {
        return crowdClient;
    }

    private Set<PlexusRole> getRoles(String userId) {
        List<String> roleNames = null;
        try {
            roleNames = crowdClient.getNexusRoles(userId);
        } catch (RemoteException e) {
            getLogger().error("Unable to look up user " + userId, e);
            return null;
        } catch (InvalidAuthorizationTokenException e) {
            getLogger().error("Unable to look up user " + userId, e);
            return null;
        } catch (ObjectNotFoundException e) {
            return null;
        }
        return convertToPlexusRoles(roleNames);
    }
}
