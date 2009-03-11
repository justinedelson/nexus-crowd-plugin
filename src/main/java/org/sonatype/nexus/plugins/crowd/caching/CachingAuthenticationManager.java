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
/**
 * 
 */
package org.sonatype.nexus.plugins.crowd.caching;

import java.rmi.RemoteException;

import com.atlassian.crowd.integration.authentication.PrincipalAuthenticationContext;
import com.atlassian.crowd.integration.authentication.ValidationFactor;
import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.service.cache.AuthenticationManagerImpl;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.integration.util.Assert;

/**
 * Implementation of Crowd client's AuthenticationManager which caches tokens
 * from a username/password authentication request.
 * 
 * @author Justin Edelson
 * 
 */
public class CachingAuthenticationManager extends AuthenticationManagerImpl {

    private AuthBasicCache basicCache;

    public CachingAuthenticationManager(SecurityServerClient securityServerClient,
            AuthBasicCache basicCache) {
        super(securityServerClient);
        this.basicCache = basicCache;
    }

    public String authenticate(PrincipalAuthenticationContext authenticationContext)
            throws RemoteException, InvalidAuthorizationTokenException,
            InvalidAuthenticationException, InactiveAccountException,
            ApplicationAccessDeniedException {
        return super.authenticate(authenticationContext);
    }

    public String authenticate(String username, String password) throws RemoteException,
            InvalidAuthorizationTokenException, InvalidAuthenticationException,
            InactiveAccountException, ApplicationAccessDeniedException {
        Assert.notNull(username);
        Assert.notNull(password);

        String token = basicCache.getToken(username, password);
        if (token == null) {
            token = super.authenticate(username, password);
            basicCache.addOrReplaceToken(username, password, token);
        }
        return token;
    }

    public void invalidate(String token) throws RemoteException, InvalidAuthorizationTokenException {
        super.invalidate(token);

    }

    public boolean isAuthenticated(String token, ValidationFactor[] validationFactors)
            throws RemoteException, InvalidAuthorizationTokenException,
            ApplicationAccessDeniedException {
        return super.isAuthenticated(token, validationFactors);
    }

}
