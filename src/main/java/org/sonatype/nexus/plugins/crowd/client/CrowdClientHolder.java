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
/**
 *
 */
package org.sonatype.nexus.plugins.crowd.client;

import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;

import com.atlassian.crowd.service.AuthenticationManager;
import com.atlassian.crowd.service.GroupManager;
import com.atlassian.crowd.service.GroupMembershipManager;
import com.atlassian.crowd.service.UserManager;
import com.atlassian.crowd.service.soap.client.SecurityServerClient;

/**
 * Interface that allows the various client components of the Crowd plugin to
 * access the Crowd client library.
 *
 * @author Justin Edelson
 *
 */
public interface CrowdClientHolder {
    public SecurityServerClient getSecurityServerClient();

    public UserManager getUserManager();

    public GroupManager getGroupManager();

    public GroupMembershipManager getGroupMembershipManager();

    public AuthenticationManager getAuthenticationManager();

    public NexusRoleManager getNexusRoleManager();

    public Configuration getConfiguration();

    public boolean isConfigured();
}
