/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.client.CrowdClientHolder;

import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;

@Component(role = Realm.class, hint = "Crowd")
public class CrowdAuthenticatingRealm extends AuthorizingRealm implements Initializable, Disposable {

    private static boolean active;

    @Requirement
    private CrowdClientHolder crowdClientHolder;

    private Logger logger = LoggerFactory.getLogger(CrowdAuthenticatingRealm.class);

    public static boolean isActive() {
        return active;
    }

    public void dispose() {
        active = false;
        logger.info("Crowd Realm deactivated...");
    }

    @Override
    public String getName() {
        return CrowdAuthenticatingRealm.class.getName();
    }

    public void initialize() throws InitializationException {
        logger.info("Crowd Realm activated...");
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
            crowdClientHolder.getAuthenticationManager()
                    .authenticate(token.getUsername(), password);
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
        String username = (String)principals.getPrimaryPrincipal();
        try {
			List<String> roles = crowdClientHolder.getNexusRoleManager().getNexusRoles(username);
			return new SimpleAuthorizationInfo(new HashSet<String>(roles));
		} catch (RemoteException e) {
			throw new AuthorizationException("Could not retrieve info from Crowd.", e);
		} catch (InvalidAuthorizationTokenException e) {
			throw new AuthorizationException("Could not retrieve info from Crowd.", e);
		} catch (ObjectNotFoundException e) {
			throw new UnknownAccountException("User " + username + " not found", e);
		}
    }

}
