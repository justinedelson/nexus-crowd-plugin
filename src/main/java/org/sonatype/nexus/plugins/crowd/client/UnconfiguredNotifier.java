/**
 *
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
