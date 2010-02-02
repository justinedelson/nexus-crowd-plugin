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
package org.sonatype.nexus.plugins.crowd.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class logs the unconfigured message once per session.
 *
 * @author justinedelson
 *
 */
class UnconfiguredNotifier {

    static Logger logger = LoggerFactory.getLogger(UnconfiguredNotifier.class);

    static boolean notified = false;

    static void unconfigured() {
        if (!notified) {
            logger.warn("Crowd plugin is not configured. This will only be logged once.");
            notified = true;
        }
    }

}
