package org.sonatype.nexus.plugins.crowd.config;

import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.nexus.proxy.events.EventMulticaster;

/**
 * Interface that manages Crowd Plugin Configuration data.
 * 
 * @author jedelson
 * 
 */
public interface CrowdPluginConfiguration extends EventMulticaster {

    Configuration getConfiguration();

    void save();

}
