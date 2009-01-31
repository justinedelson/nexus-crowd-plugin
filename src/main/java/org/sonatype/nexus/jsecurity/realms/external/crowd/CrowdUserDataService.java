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

import java.rmi.RemoteException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.MissingAccountException;
import org.sonatype.nexus.jsecurity.realms.external.ExternalUserDataService;
import org.sonatype.nexus.plugins.crowd.client.CrowdClient;

import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;

@Component(role = ExternalUserDataService.class, hint = "crowd")
public class CrowdUserDataService extends AbstractLogEnabled implements ExternalUserDataService {

    @Requirement
    private CrowdClient crowdClient;

    public AuthenticationInfo authenticate(UsernamePasswordToken token, String name) {
        try {
            crowdClient.authenticatePrincipalSimple(token.getUsername(), new String(token
                    .getPassword()));
            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), name);
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

    public List<String> getRoles(String username) {
        try {
            return crowdClient.getNexusRoles(username);
        } catch (RemoteException e) {
            throw new AuthorizationException("Unable to connect to Crowd.", e);
        } catch (InvalidAuthorizationTokenException e) {
            throw new AuthorizationException("Unable to connect to Crowd.", e);
        } catch (ObjectNotFoundException e) {
            throw new MissingAccountException("User '" + username + "' cannot be retrieved.", e);
        }
    }

}
