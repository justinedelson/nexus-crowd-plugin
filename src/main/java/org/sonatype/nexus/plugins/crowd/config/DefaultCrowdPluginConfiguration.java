package org.sonatype.nexus.plugins.crowd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.io.xpp3.NexusCrowdPluginConfigurationXpp3Reader;
import org.sonatype.nexus.proxy.EventMulticasterComponent;

@Component(role = CrowdPluginConfiguration.class, hint = "default")
public class DefaultCrowdPluginConfiguration extends EventMulticasterComponent implements
        CrowdPluginConfiguration {

    @org.codehaus.plexus.component.annotations.Configuration(value = "${nexus-work}/conf/crowd-plugin.xml")
    private File configurationFile;

    private Configuration configuration;

    private ReentrantLock lock = new ReentrantLock();

    public Configuration getConfiguration() {
        if (configuration != null) {
            return configuration;
        }

        lock.lock();

        FileInputStream is = null;

        try {
            is = new FileInputStream(configurationFile);

            NexusCrowdPluginConfigurationXpp3Reader reader = new NexusCrowdPluginConfigurationXpp3Reader();

            configuration = reader.read(is);
        } catch (FileNotFoundException e) {
            getLogger().error(
                    "Crowd configuration file does not exist: "
                            + configurationFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().error("IOException while retrieving configuration file", e);
        } catch (XmlPullParserException e) {
            getLogger().error("Invalid XML Configuration", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // just closing if open
                }
            }

            lock.unlock();
        }

        return configuration;
    }

    public void save() {
        // TODO
    }
}
