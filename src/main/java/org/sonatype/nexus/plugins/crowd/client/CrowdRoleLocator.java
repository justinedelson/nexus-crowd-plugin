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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;

/**
 * Implementation of the PlexusRoleLocator interface that is backed by an
 * instance of Atlassian Crowd.
 * 
 * @author Justin Edelson
 * 
 */
@Component(role = PlexusRoleLocator.class, hint = "Crowd")
public class CrowdRoleLocator extends BaseCrowdLocator implements PlexusRoleLocator {

    public String getSource() {
        return SOURCE;
    }

    public Set<String> listRoleIds() {
        try {
            List<String> roleNames = getCrowdClientHolder().getNexusRoleManager()
                    .getAllNexusRoles();
            return new HashSet<String>(roleNames);
        } catch (Exception e) {
            getLogger().error("Unable to list all role ids", e);
            return Collections.emptySet();
        }
    }

    public Set<PlexusRole> listRoles() {
        try {
            List<String> roleNames = getCrowdClientHolder().getNexusRoleManager()
                    .getAllNexusRoles();
            return convertToPlexusRoles(roleNames);
        } catch (Exception e) {
            getLogger().error("Unable to list all role ids", e);
            return Collections.emptySet();
        }
    }

}
