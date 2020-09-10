/**********************************************************************************************************************
 * vmcat                                                                                                              *
 *                                                                                                                    *
 * Copyright (c) 2020 Mike Millson                                                                                    *
 *                                                                                                                    * 
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License       * 
 * v. 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0 which is    *
 * available at https://www.apache.org/licenses/LICENSE-2.0.                                                          *
 *                                                                                                                    *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0                                                                     *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.github.vmcat.util.jdk;

import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.domain.UnknownEvent;
import org.github.vmcat.domain.jdk.RevokeBiasEvent;

/**
 * <p>
 * Utility methods and constants.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JdkUtil {

    /**
     * Defined logging events.
     */
    public enum LogEventType {
        //
        REVOKE_BIAS, UNKNOWN
    };

    /**
     * Create <code>LogEvent</code> from GC log line.
     * 
     * @param logLine
     *            The log line as it appears in the VM log.
     * @return The <code>LogEvent</code> corresponding to the log line.
     */
    public static final LogEvent parseLogLine(String logLine) {
        LogEventType eventType = identifyEventType(logLine);
        LogEvent event = null;
        switch (eventType) {

        case REVOKE_BIAS:
            event = new RevokeBiasEvent(logLine);
            break;

        case UNKNOWN:
            event = new UnknownEvent(logLine);
            break;

        default:
            throw new AssertionError("Unexpected event type value: " + eventType);
        }
        return event;
    }

    /**
     * Identify the log line VM event.
     * 
     * @param logLine
     *            The log entry.
     * @return The <code>LogEventType</code> of the log entry.
     */
    public static final LogEventType identifyEventType(String logLine) {
        if (RevokeBiasEvent.match(logLine))
            return LogEventType.REVOKE_BIAS;

        // no idea what event is
        return LogEventType.UNKNOWN;
    }

    /**
     * Create <code>SafepointEvent</code> from values.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @param logEntry
     *            Log entry.
     * @return The <code>SafepointEvent</code> for the given event values.
     */
    public static final SafepointEvent hydrateSafepointEvent(LogEventType eventType, String logEntry) {
        SafepointEvent event = null;
        switch (eventType) {

        case REVOKE_BIAS:
            event = new RevokeBiasEvent(logEntry);
            break;

        default:
            throw new AssertionError("Unexpected event type value: " + eventType + ": " + logEntry);
        }
        return event;
    }

    /**
     * @param eventType
     *            The event type to test.
     * @return true if the log event is should be included in the report event list, false otherwise.
     */
    public static final boolean isReportable(LogEventType eventType) {

        boolean reportable = true;

        switch (eventType) {
        case UNKNOWN:
            reportable = false;
            break;
        default:
            break;
        }

        return reportable;
    }

}
