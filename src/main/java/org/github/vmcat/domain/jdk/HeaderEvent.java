/**********************************************************************************************************************
 * vmcat                                                                                                              *
 *                                                                                                                    *
 * Copyright (c) 2020-2023 Mike Millson                                                                                    *
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
import org.github.vmcat.util.jdk.JdkUtil;

/**
 * <p>
 * HEADER
 * </p>
 * 
 * <p>
 * Header with field definitions that precedes every <code>SafepointEvent</code> log line.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * JDK8:
 * </p>
 * 
 * <pre>
 *          vmop                    [threads: total initially_running wait_to_block]    [time: spin block sync cleanup vmop] page_trap_count
 * </pre>
 * 
 * <p>
 * JDK11:
 * </p>
 * 
 * <pre>
 *           vmop                            [ threads:    total initially_running wait_to_block ][ time:    spin   block    sync cleanup    vmop ] page_trap_count
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^[ ]{9,10}vmop[ ]{18,28}\\[[ ]{0,1}threads:[ ]{1,4}total initially_running "
            + "wait_to_block[ ]{0,1}\\][ ]{0,4}\\[[ ]{0,1}time:[ ]{1,4}spin[ ]{1,3}block[ ]{1,4}sync cleanup[ ]{1,4}"
            + "vmop[ ]{0,1}\\] page_trap_count$";

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
    public HeaderEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEADER.toString();
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
        return logLine.matches(REGEX);
    }
}
