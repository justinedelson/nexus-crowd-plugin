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
package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPEntity;

/**
 * Adapter interface that allows Nexus to switch between Crowd groups and Crowd
 * roles.
 *
 * @author Justin Edelson
 */
public interface NexusRoleManager {

    List<String> getNexusRoles(String username) throws UserNotFoundException, RemoteException, InvalidAuthenticationException, InvalidAuthorizationTokenException;

    SOAPEntity getNexusRole(String roleName) throws GroupNotFoundException, RemoteException, InvalidAuthenticationException, InvalidAuthorizationTokenException;

    List<String> getAllNexusRoles() throws RemoteException, InvalidAuthenticationException, InvalidAuthorizationTokenException;

}
