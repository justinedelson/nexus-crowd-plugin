package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;

import com.atlassian.crowd.integration.authentication.PasswordCredential;
import com.atlassian.crowd.integration.authentication.PrincipalAuthenticationContext;
import com.atlassian.crowd.integration.authentication.ValidationFactor;
import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.ApplicationPermissionException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.InvalidCredentialException;
import com.atlassian.crowd.integration.exception.InvalidEmailAddressException;
import com.atlassian.crowd.integration.exception.InvalidGroupException;
import com.atlassian.crowd.integration.exception.InvalidPrincipalException;
import com.atlassian.crowd.integration.exception.InvalidRoleException;
import com.atlassian.crowd.integration.exception.InvalidTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.service.soap.client.ClientProperties;
import com.atlassian.crowd.integration.service.soap.client.ClientPropertiesImpl;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClientImpl;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPCookieInfo;
import com.atlassian.crowd.integration.soap.SOAPGroup;
import com.atlassian.crowd.integration.soap.SOAPNestableGroup;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;
import com.atlassian.crowd.integration.soap.SOAPRole;
import com.atlassian.crowd.integration.soap.SearchRestriction;

@Component(role = CrowdClient.class, hint = "default")
public class DefaultCrowdClient extends AbstractLogEnabled implements Serviceable, CrowdClient,
        Initializable {

    private Configuration configuration;

    private SecurityServerClient crowdClient;

    @Requirement
    private CrowdPluginConfiguration crowdPluginConfiguration;

    private ServiceLocator locator;

    public void addAttributeToPrincipal(String principal, SOAPAttribute attribute)
            throws RemoteException, InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.addAttributeToPrincipal(principal, attribute);
    }

    public SOAPGroup addGroup(SOAPGroup group) throws RemoteException, InvalidGroupException,
            InvalidAuthorizationTokenException, ApplicationPermissionException {
        return crowdClient.addGroup(group);
    }

    public SOAPPrincipal addPrincipal(SOAPPrincipal principal, PasswordCredential credential)
            throws RemoteException, InvalidAuthorizationTokenException, InvalidCredentialException,
            InvalidPrincipalException, ApplicationPermissionException {
        return crowdClient.addPrincipal(principal, credential);
    }

    public void addPrincipalToGroup(String principal, String group) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.addPrincipalToGroup(principal, group);
    }

    public void addPrincipalToRole(String principal, String role) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.addPrincipalToRole(principal, role);
    }

    public SOAPRole addRole(SOAPRole role) throws RemoteException,
            InvalidAuthorizationTokenException, InvalidRoleException,
            ApplicationPermissionException {
        return crowdClient.addRole(role);
    }

    public void authenticate() throws RemoteException, InvalidAuthorizationTokenException {
        crowdClient.authenticate();
    }

    public String authenticatePrincipal(
            PrincipalAuthenticationContext principalAuthenticationContext) throws RemoteException,
            InvalidAuthorizationTokenException, InvalidAuthenticationException,
            InactiveAccountException, ApplicationAccessDeniedException {
        return crowdClient.authenticatePrincipal(principalAuthenticationContext);
    }

    public String authenticatePrincipalSimple(String username, String password)
            throws RemoteException, InvalidAuthorizationTokenException,
            InvalidAuthenticationException, InactiveAccountException,
            ApplicationAccessDeniedException {
        return crowdClient.authenticatePrincipalSimple(username, password);
    }

    public String createPrincipalToken(String username, ValidationFactor[] validationFactors)
            throws RemoteException, InvalidAuthorizationTokenException,
            InvalidAuthenticationException, InactiveAccountException,
            ApplicationAccessDeniedException {
        return crowdClient.createPrincipalToken(username, validationFactors);
    }

    public String[] findAllGroupNames() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.findAllGroupNames();
    }

    public SOAPNestableGroup[] findAllGroupRelationships() throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.findAllGroupRelationships();
    }

    public SOAPGroup[] findAllGroups() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.findAllGroups();
    }

    public String[] findAllPrincipalNames() throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.findAllPrincipalNames();
    }

    public SOAPPrincipal[] findAllPrincipals() throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.findAllPrincipals();
    }

    public String[] findAllRoleNames() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.findAllRoleNames();
    }

    public SOAPRole[] findAllRoles() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.findAllRoles();
    }

    public SOAPGroup findGroupByName(String name) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        return crowdClient.findGroupByName(name);
    }

    public String[] findGroupMemberships(String principalName) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        return crowdClient.findGroupMemberships(principalName);
    }

    public SOAPPrincipal findPrincipalByName(String name) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        return crowdClient.findPrincipalByName(name);
    }

    public SOAPPrincipal findPrincipalByToken(String key) throws RemoteException,
            InvalidTokenException, InvalidAuthorizationTokenException {
        return crowdClient.findPrincipalByToken(key);
    }

    public SOAPRole findRoleByName(String name) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        return crowdClient.findRoleByName(name);
    }

    public String[] findRoleMemberships(String principalName) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        return crowdClient.findRoleMemberships(principalName);
    }

    public List<String> getAllNexusRoles() throws RemoteException,
            InvalidAuthorizationTokenException {
        List<String> roles;
        if (configuration.isUseGroups()) {
            roles = Arrays.asList(crowdClient.findAllGroupNames());
        } else {
            roles = Arrays.asList(crowdClient.findAllRoleNames());
        }
        return roles;
    }

    public long getCacheTime() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.getCacheTime();
    }

    public ClientProperties getClientProperties() {
        return crowdClient.getClientProperties();
    }

    public SOAPCookieInfo getCookieInfo() throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.getCookieInfo();
    }

    public String getDomain() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.getDomain();
    }

    public String[] getGrantedAuthorities() throws InvalidAuthorizationTokenException,
            RemoteException {
        return crowdClient.getGrantedAuthorities();
    }

    public List<String> getNexusRoles(String username) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        getLogger().debug("Looking up role list for username: " + username);

        List<String> roles;
        if (configuration.isUseGroups()) {
            roles = Arrays.asList(crowdClient.findGroupMemberships(username));
        } else {
            roles = Arrays.asList(crowdClient.findRoleMemberships(username));
        }

        getLogger().debug("Obtained role list: " + roles.toString());

        return roles;

    }

    public void initialize() throws InitializationException {
        configuration = crowdPluginConfiguration.getConfiguration();
        ClientProperties clientProps = new ClientPropertiesImpl(configuration
                .getCrowdClientProperties());
        crowdClient = new SecurityServerClientImpl(clientProps);
    }

    public void invalidateToken(String token) throws RemoteException,
            InvalidAuthorizationTokenException {
        crowdClient.invalidateToken(token);
    }

    public boolean isCacheEnabled() throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.isCacheEnabled();
    }

    public boolean isGroupMember(String group, String principal) throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.isGroupMember(group, principal);
    }

    public boolean isRoleMember(String role, String principal) throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.isRoleMember(role, principal);
    }

    public boolean isValidToken(String principalToken, ValidationFactor[] validationFactors)
            throws RemoteException, InvalidAuthorizationTokenException,
            ApplicationAccessDeniedException {
        return crowdClient.isValidToken(principalToken, validationFactors);
    }

    public void removeAttributeFromPrincipal(String principal, String attribute)
            throws RemoteException, InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.removeAttributeFromPrincipal(principal, attribute);
    }

    public void removeGroup(String group) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.removeGroup(group);
    }

    public void removePrincipal(String principal) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.removePrincipal(principal);
    }

    public void removePrincipalFromGroup(String principal, String group) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.removePrincipalFromGroup(principal, group);
    }

    public void removePrincipalFromRole(String principal, String role) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException,
            ApplicationPermissionException {
        crowdClient.removePrincipalFromRole(principal, role);
    }

    public void removeRole(String role) throws RemoteException, InvalidAuthorizationTokenException,
            ObjectNotFoundException, ApplicationPermissionException {
        crowdClient.removeRole(role);
    }

    public void resetPrincipalCredential(String principal) throws RemoteException,
            InvalidEmailAddressException, InvalidCredentialException, ObjectNotFoundException,
            ApplicationPermissionException, InvalidAuthorizationTokenException {
        crowdClient.resetPrincipalCredential(principal);
    }

    public SOAPGroup[] searchGroups(SearchRestriction[] searchRestrictions) throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.searchGroups(searchRestrictions);
    }

    public SOAPPrincipal[] searchPrincipals(SearchRestriction[] searchRestrictions)
            throws RemoteException, InvalidAuthorizationTokenException {
        return crowdClient.searchPrincipals(searchRestrictions);
    }

    public SOAPRole[] searchRoles(SearchRestriction[] searchRestrictions) throws RemoteException,
            InvalidAuthorizationTokenException {
        return crowdClient.searchRoles(searchRestrictions);
    }

    public void service(ServiceLocator locator) {
        this.locator = locator;
    }

    public void updateGroup(String group, String description, boolean active)
            throws RemoteException, ObjectNotFoundException, ApplicationPermissionException,
            InvalidAuthorizationTokenException {
        crowdClient.updateGroup(group, description, active);
    }

    public void updatePrincipalAttribute(String name, SOAPAttribute attribute)
            throws RemoteException, ObjectNotFoundException, ApplicationPermissionException,
            InvalidAuthorizationTokenException {
        crowdClient.updatePrincipalAttribute(name, attribute);
    }

    public void updatePrincipalCredential(String principal, PasswordCredential credential)
            throws RemoteException, InvalidAuthorizationTokenException, InvalidCredentialException,
            ObjectNotFoundException, ApplicationPermissionException {
        crowdClient.updatePrincipalCredential(principal, credential);
    }

}
