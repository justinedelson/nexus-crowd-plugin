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
package org.sonatype.nexus.plugins.crowd.client;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.plugins.crowd.caching.AuthBasicCache;
import org.sonatype.nexus.plugins.crowd.caching.AuthCacheImpl;
import org.sonatype.nexus.plugins.crowd.caching.CachingAuthenticationManager;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;

import com.atlassian.crowd.integration.service.AuthenticationManager;
import com.atlassian.crowd.integration.service.GroupManager;
import com.atlassian.crowd.integration.service.GroupMembershipManager;
import com.atlassian.crowd.integration.service.UserManager;
import com.atlassian.crowd.integration.service.cache.CachingGroupManager;
import com.atlassian.crowd.integration.service.cache.CachingGroupMembershipManager;
import com.atlassian.crowd.integration.service.cache.CachingUserManager;
import com.atlassian.crowd.integration.service.soap.client.ClientProperties;
import com.atlassian.crowd.integration.service.soap.client.ClientPropertiesImpl;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClientImpl;

/**
 * Implementation of the CrowdClientHolder which uses caching wherever possible.
 * 
 * @author Justin Edelson
 * 
 */
@Component(role = CrowdClientHolder.class, hint = "default")
public class DefaultCrowdClientHolder extends AbstractLogEnabled implements CrowdClientHolder,
        Initializable {

    private AuthenticationManager authenticationManager;

    private AuthBasicCache basicCache;

    @Requirement
    private PlexusEhCacheWrapper cacheManager;

    private Configuration configuration;

    @Requirement
    private CrowdPluginConfiguration crowdPluginConfiguration;

    private GroupManager groupManager;

    private GroupMembershipManager groupMembershipManager;

    private NexusRoleManager nexusRoleManager;

    private SecurityServerClient securityServerClient;

    private UserManager userManager;

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public GroupMembershipManager getGroupMembershipManager() {
        return groupMembershipManager;
    }

    public NexusRoleManager getNexusRoleManager() {
        return nexusRoleManager;
    }

    public SecurityServerClient getSecurityServerClient() {
        return securityServerClient;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void initialize() throws InitializationException {
        basicCache = new AuthCacheImpl(cacheManager.getEhCacheManager());
        configuration = crowdPluginConfiguration.getConfiguration();
        ClientProperties clientProps = new ClientPropertiesImpl(configuration
                .getCrowdClientProperties());
        securityServerClient = new SecurityServerClientImpl(clientProps);
        userManager = new CachingUserManager(securityServerClient, basicCache);
        groupManager = new CachingGroupManager(securityServerClient, basicCache);
        groupMembershipManager = new CachingGroupMembershipManager(securityServerClient,
                userManager, groupManager, basicCache);
        authenticationManager = new CachingAuthenticationManager(securityServerClient, basicCache);
        nexusRoleManager = new DefaultNexusRoleManager(configuration.isUseGroups(), groupManager,
                groupMembershipManager, securityServerClient);

    }

}
