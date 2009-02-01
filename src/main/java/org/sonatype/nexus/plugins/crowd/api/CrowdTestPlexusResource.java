package org.sonatype.nexus.plugins.crowd.api;

import java.rmi.RemoteException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.crowd.client.CrowdClient;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;

/**
 * Intent of this class is to enable an admin to easily test if the Crowd
 * connection is working <b>without</b> enabling the Realm.
 * 
 * But it doesn't work (yet).
 * 
 * @author Justin Edelson
 * 
 */
@Component(role = PlexusResource.class, hint = "CrowdTestPlexusResource")
public class CrowdTestPlexusResource extends AbstractPlexusResource {

    @Requirement
    private CrowdClient crowdClient;

    @Override
    public Object getPayloadInstance() {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection() {
        return new PathProtectionDescriptor("/crowd/test", "anon");
    }

    @Override
    public String getResourceUri() {
        return "/crowd/test";
    }

    @Override
    public Object get(Context context, Request request, Response response, Variant variant)
            throws ResourceException {
        try {
            crowdClient.authenticate();
            return "<status>OK</status>";
        } catch (RemoteException e) {
            throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    "Unable to authenticate. Check configuration.", e);
        } catch (InvalidAuthorizationTokenException e) {
            throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    "Unable to authenticate. Check configuration.", e);
        }
    }
}
