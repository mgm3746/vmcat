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

import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.util.jdk.JdkUtil;

/**
 * <p>
 * REVOKE_BIAS
 * </p>
 * 
 * <p>
 * Biased locking is an optimization intended to reduce the overhead of uncontested locking. It assumes a thread owns a
 * monitor until another thread tries to acquire it.
 * </p>
 * 
 * <p>
 * RevokeBias it the operation the JVM does when a different thread tries to acquire the monitor.
 * </p>
 * 
 * <p>
 * BiasedLocking is being disabled and deprecated in JDK 17: https://bugs.openjdk.java.net/browse/JDK-8231265.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 1652.991: RevokeBias                       [    2403          0             13    ]      [     0     0     2    29     0    ]  0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class RevokeBiasEvent implements SafepointEvent {

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The total number of threads stopped in safepoint.
     */
    private int threadsTotal;

    /**
     * The number of threads that were spinning before safepoint.
     */
    int threadsSpinning;

    /**
     * The number of threads that were blocked before safepoint.
     */
    int threadsBlocked;

    /**
     * The time it took spinning threads to reach safepoint in milliseconds.
     */
    int timeSpin;

    /**
     * The time it took blocked threads to reach safepoint in milliseconds.
     */
    int timeBlock;

    /**
     * The time it took all threads to reach safepoint in milliseconds.
     */
    int timeSync;

    /**
     * The time it took for cleanup activities in milliseconds.
     */
    int timeCleanup;

    /**
     * The time it took for the safepoint activity in milliseconds.
     */
    int timeVmop;

    /**
     * The page trap count.
     */
    int pageTrapCount;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public RevokeBiasEvent(String logEntry) {

    }

    public String getName() {
        return JdkUtil.LogEventType.REVOKE_BIAS.toString();
    }

    public String getLogEntry() {
        return logEntry;
    }

    protected void setLogEntry(String logEntry) {
        this.logEntry = logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^TBD$";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return logLine.matches(REGEX);
    }

    public int getThreadsTotal() {
        return threadsTotal;
    }

    public void setThreadsTotal(int threadsTotal) {
        this.threadsTotal = threadsTotal;
    }

    public int getThreadsSpinning() {
        return threadsSpinning;
    }

    public void setThreadsSpinning(int threadsSpinning) {
        this.threadsSpinning = threadsSpinning;
    }

    public int getThreadsBlocked() {
        return threadsBlocked;
    }

    public void setThreadsBlocked(int threadsBlocked) {
        this.threadsBlocked = threadsBlocked;
    }

    public int getTimeSpin() {
        return timeSpin;
    }

    public void setTimeSpin(int timeSpin) {
        this.timeSpin = timeSpin;
    }

    public int getTimeBlock() {
        return timeBlock;
    }

    public void setTimeBlock(int timeBlock) {
        this.timeBlock = timeBlock;
    }

    public int getTimeSync() {
        return timeSync;
    }

    public void setTimeSync(int timeSync) {
        this.timeSync = timeSync;
    }

    public int getTimeCleanup() {
        return timeCleanup;
    }

    public void setTimeCleanup(int timeCleanup) {
        this.timeCleanup = timeCleanup;
    }

    public int getTimeVmop() {
        return timeVmop;
    }

    public void setTimeVmop(int timeVmop) {
        this.timeVmop = timeVmop;
    }

    public int getPageTrapCount() {
        return pageTrapCount;
    }

    public void setPageTrapCount(int pageTrapCount) {
        this.pageTrapCount = pageTrapCount;
    }

}
