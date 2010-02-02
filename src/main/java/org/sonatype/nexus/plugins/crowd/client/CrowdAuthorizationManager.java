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

import java.rmi.RemoteException;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.soap.SOAPEntity;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author justin
 *
 */
@Component(role = AuthorizationManager.class, hint = "Crowd")
public class CrowdAuthorizationManager extends AbstractReadOnlyAuthorizationManager {

    @Requirement
    private CrowdClientHolder crowdClientHolder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CrowdAuthorizationManager() {
        logger.info("CrowdAuthorizationManager is starting...");
    }

    /**
     * {@inheritDoc}
     */
    public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
        throw new NoSuchPrivilegeException("Crowd plugin doesn't support privleges");
    }

    /**
     * {@inheritDoc}
     */
    public Role getRole(String roleId) throws NoSuchRoleException {
        SOAPEntity entity = null;
        try {
            entity = crowdClientHolder.getNexusRoleManager().getNexusRole(roleId);
        } catch (Exception e) {
            throw new NoSuchRoleException("Failed to get role " + roleId + " from Crowd.", e);
        }

        return convertFromEntityToRole.apply(entity);

    }

    private Function<SOAPEntity, Role> convertFromEntityToRole = new Function<SOAPEntity, Role>() {

        public Role apply(SOAPEntity entity) {
            Role role = new Role();
            role.setRoleId(entity.getName());
            role.setName(entity.getName());
            role.setDescription(entity.getDescription());
            role.setSource(CrowdUserManager.SOURCE);
            role.setReadOnly(true);
            role.setSessionTimeout(60); // no longer used

            return role;
        }
    };

    private Function<String, Role> convertFromNameToRole = new Function<String, Role>() {

        public Role apply(String name) {
            Role role = new Role();
            role.setRoleId(name);
            role.setName(name);
            role.setDescription(name);
            role.setSource(CrowdUserManager.SOURCE);
            role.setReadOnly(true);
            role.setSessionTimeout(60); // no longer used

            return role;
        }
    };

    /**
     * {@inheritDoc}
     */
    public String getSource() {
        return CrowdUserManager.SOURCE;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Privilege> listPrivileges() {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    public Set<Role> listRoles() {
        if (crowdClientHolder.isConfigured()) {
            try {
                return Sets.newHashSet(Iterables.transform(crowdClientHolder.getNexusRoleManager().getAllNexusRoles(),
                        convertFromNameToRole));
            } catch (RemoteException e) {
                logger.error("Unable to load roles", e);
                return null;
            } catch (InvalidAuthorizationTokenException e) {
                logger.error("Unable to load roles", e);
                return null;
            }
        }
        UnconfiguredNotifier.unconfigured();
        return Collections.emptySet();
    }

}
