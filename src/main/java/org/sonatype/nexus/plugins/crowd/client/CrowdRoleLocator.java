package org.sonatype.nexus.plugins.crowd.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;

import edu.emory.mathcs.backport.java.util.Collections;

@Component(role = PlexusRoleLocator.class, hint = "Crowd")
public class CrowdRoleLocator extends BaseCrowdLocator implements PlexusRoleLocator {

    public String getSource() {
        return SOURCE;
    }

    public Set<String> listRoleIds() {
        try {
            List<String> roleNames = getCrowdClient().getAllNexusRoles();
            return new HashSet<String>(roleNames);
        } catch (Exception e) {
            getLogger().error("Unable to list all role ids", e);
            return Collections.emptySet();
        }
    }

    public Set<PlexusRole> listRoles() {
        try {
            List<String> roleNames = getCrowdClient().getAllNexusRoles();
            return convertToPlexusRoles(roleNames);
        } catch (Exception e) {
            getLogger().error("Unable to list all role ids", e);
            return Collections.emptySet();
        }
    }

}
