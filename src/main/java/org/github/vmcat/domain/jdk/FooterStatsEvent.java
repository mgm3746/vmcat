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
package org.github.vmcat.domain.jdk;

import org.github.vmcat.domain.ThrowAwayEvent;
import org.github.vmcat.util.jdk.JdkRegEx;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.Safepoint;

/**
 * <p>
 * FOOTER_STATS
 * </p>
 * 
 * <p>
 * Stats printed at the end of vm logging.
 * </p>
 * 
 * <pre>
 * Polling page always armed
 * Deoptimize                         1
 * GenCollectForAllocation        10273
 * EnableBiasedLocking                1
 *     0 VM operations coalesced during safepoint
 * Maximum sync time      0 ms
 * Maximum vm operation time (except for Exit VM operation)     23 ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FooterStatsEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            "^Polling page always armed$",
            //
            "^" + Safepoint.triggerRegEx() + "[ ]{1,32}" + JdkRegEx.NUMBER + "$",
            //
            "^[ ]{0,4} " + JdkRegEx.NUMBER + " VM operations coalesced during safepoint",
            //
            "^Maximum sync time[ ]{1,6} \\d{1,6} ms",
            //
            "^Maximum vm operation time \\(except for Exit VM operation\\)[ ]{1,5} \\d{1,6} ms$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the VM event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public FooterStatsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.FOOTER_STATS.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}
