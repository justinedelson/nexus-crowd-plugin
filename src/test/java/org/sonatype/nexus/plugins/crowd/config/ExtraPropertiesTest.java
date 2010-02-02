/**
 *
 */
package org.sonatype.nexus.plugins.crowd.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Property;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.io.xpp3.NexusCrowdPluginConfigurationXpp3Reader;

public class ExtraPropertiesTest {

    @Test
    public void test() throws Exception {
        NexusCrowdPluginConfigurationXpp3Reader reader = new NexusCrowdPluginConfigurationXpp3Reader();

        Configuration configuration = reader.read(getClass().getResourceAsStream("/extra-props.xml"));

        List props = configuration.getExtraCrowdProperties();
        Assert.assertEquals(1, props.size());

        Property prop = (Property) props.get(0);
        Assert.assertEquals("http.timeout", prop.getKey());
        Assert.assertEquals("30000", prop.getValue());

    }
}
