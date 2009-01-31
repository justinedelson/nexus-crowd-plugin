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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.pam.UnsupportedTokenException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.nexus.plugins.crowd.client.CrowdClient;

import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;

@Component(role = Realm.class, hint = "Crowd")
public class CrowdAuthenticatingRealm extends AuthorizingRealm implements Initializable, Disposable {

    private static boolean active;

    @Requirement
    private CrowdClient crowdClient;

    public static boolean isActive() {
        return active;
    }

    public void dispose() {
        active = false;
        System.out.println("Crowd Realm deactivated...");
    }

    @Override
    public String getName() {
        return CrowdAuthenticatingRealm.class.getName();
    }

    public void initialize() throws InitializationException {
        System.out.println("Crowd Realm activated...");
        active = true;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        if (!(authenticationToken instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException("Token of type "
                    + authenticationToken.getClass().getName() + " is not " + "supported.  A "
                    + UsernamePasswordToken.class.getName() + " is required.");
        }
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

        String password = new String(token.getPassword());

        try {
            crowdClient.authenticatePrincipalSimple(token.getUsername(), password);
            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(),
                    getName());
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

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

}
