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

        List<?> props = configuration.getExtraCrowdProperties();
        Assert.assertEquals(1, props.size());

        Property prop = (Property) props.get(0);
        Assert.assertEquals("http.timeout", prop.getKey());
        Assert.assertEquals("30000", prop.getValue());

    }
}
