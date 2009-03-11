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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.service.GroupManager;
import com.atlassian.crowd.integration.service.GroupMembershipManager;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;

public class DefaultNexusRoleManager implements NexusRoleManager {

    private static final Log LOGGER = LogFactory.getLog(DefaultNexusRoleManager.class);

    private boolean useGroups;
    private GroupManager groupManager;
    private GroupMembershipManager groupMembershipManager;
    private SecurityServerClient securityServerClient;

    public DefaultNexusRoleManager(boolean useGroups, GroupManager groupManager,
            GroupMembershipManager groupMembershipManager, SecurityServerClient securityServerClient) {
        this.useGroups = useGroups;
        this.groupManager = groupManager;
        this.groupMembershipManager = groupMembershipManager;
        this.securityServerClient = securityServerClient;
    }

    public List<String> getAllNexusRoles() throws RemoteException,
            InvalidAuthorizationTokenException {
        List<String> roles;
        if (useGroups) {
            roles = groupManager.getAllGroupNames();
        } else {
            roles = Arrays.asList(securityServerClient.findAllRoleNames());
        }
        return roles;
    }

    public List<String> getNexusRoles(String username) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Looking up role list for username: " + username);
        }

        List<String> roles;
        if (useGroups) {
            roles = groupMembershipManager.getMemberships(username);
        } else {
            roles = Arrays.asList(securityServerClient.findRoleMemberships(username));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Obtained role list: " + roles.toString());
        }

        return roles;
    }

}
