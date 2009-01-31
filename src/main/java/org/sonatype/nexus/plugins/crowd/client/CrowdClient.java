package org.sonatype.nexus.plugins.crowd.client;

import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;

public interface CrowdClient extends SecurityServerClient {

    List<String> getNexusRoles(String username) throws RemoteException,
            InvalidAuthorizationTokenException, ObjectNotFoundException;

    List<String> getAllNexusRoles() throws RemoteException, InvalidAuthorizationTokenException;

}
