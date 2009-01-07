/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.MissingAccountException;
import org.sonatype.nexus.jsecurity.realms.external.ExternalRoleMapper;
import org.sonatype.nexus.jsecurity.realms.external.ExternalUserDataService;
import org.sonatype.nexus.jsecurity.realms.external.ExternalUserRoleUtils;

import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.service.soap.client.ClientProperties;
import com.atlassian.crowd.integration.service.soap.client.ClientPropertiesImpl;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClientImpl;

public class CrowdUserDataService extends AbstractLogEnabled implements ExternalUserDataService,
        Initializable, Serviceable {

    private static final String ANONYMOUS = "anonymous";

    /**
     * If you have created a custom role for the anonymous user, specify that
     * here.
     * 
     * @plexus.configuration
     */
    private String anonymousRole = ANONYMOUS;

    private SecurityServerClient crowdClient;

    /**
     * @plexus.configuration
     */
    private Properties crowdProperties;

    private ServiceLocator locator;

    /**
     * The name of the ExternalRoleMapper that will be used to map external role
     * names to Nexus role names.
     * 
     * @plexus.configuration
     */
    private String mapperName;

    /**
     * If true, Crowd groups will be used to populate the role list. If false,
     * Crowd roles will be used.
     * 
     * @plexus.configuration
     */
    private boolean useGroups;

    public AuthenticationInfo authenticate(UsernamePasswordToken token, String name) {
        if (isAnonymous(token)) {
            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), name);
        } else {
            try {
                crowdClient.authenticatePrincipalSimple(token.getUsername(), new String(token
                        .getPassword()));
                return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(),
                        name);
            } catch (RemoteException e) {
                throw new AuthenticationException("Could not retrieve info from Crowd.", e);
            } catch (InvalidAuthorizationTokenException e) {
                throw new AuthenticationException("Could not retrieve info from Crowd.", e);
            } catch (ApplicationAccessDeniedException e) {
                throw new AuthenticationException("Could not retrieve info from Crowd.", e);
            } catch (InvalidAuthenticationException e) {
                throw new IncorrectCredentialsException(e);
            } catch (InactiveAccountException e) {
                throw new DisabledAccountException(e);
            }
        }
    }

    public List<String> getRoles(String username) {
        if (ANONYMOUS.equals(username)) {
            return Arrays.asList(anonymousRole);
        } else {
            getLogger().info("Looking up role list for username: " + username);

            List<String> roles = getRoleList(username);

            try {
                if (mapperName != null && locator.hasComponent(ExternalRoleMapper.ROLE, mapperName)) {
                    ExternalRoleMapper mapper = (ExternalRoleMapper) locator.lookup(
                            ExternalRoleMapper.ROLE, mapperName);
                    roles = ExternalUserRoleUtils.mapRoles(roles, mapper);
                }
            } catch (ComponentLookupException e) {
                getLogger().error("Unable to map role names.", e);
            }

            getLogger().info("Obtained role list: " + roles.toString());

            return roles;
        }
    }

    public void initialize() throws InitializationException {
        ClientProperties clientProps = new ClientPropertiesImpl(crowdProperties);
        crowdClient = new SecurityServerClientImpl(clientProps);
    }

    public void service(ServiceLocator locator) {
        this.locator = locator;
    }

    private List<String> getRoleList(String username) {
        List<String> roles;
        try {
            if (useGroups) {
                roles = Arrays.asList(crowdClient.findGroupMemberships(username));
            } else {
                roles = Arrays.asList(crowdClient.findRoleMemberships(username));
            }
        } catch (RemoteException e) {
            throw new AuthorizationException("Unable to connect to Crowd.", e);
        } catch (InvalidAuthorizationTokenException e) {
            throw new AuthorizationException("Unable to connect to Crowd.", e);
        } catch (ObjectNotFoundException e) {
            throw new MissingAccountException("User '" + username + "' cannot be retrieved.", e);
        }
        return roles;
    }

    private boolean isAnonymous(UsernamePasswordToken token) {
        return ANONYMOUS.equals(token.getUsername())
                && ANONYMOUS.equals(new String(token.getPassword()));
    }

}
