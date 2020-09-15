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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.util.jdk.JdkMath;
import org.github.vmcat.util.jdk.JdkRegEx;
import org.github.vmcat.util.jdk.JdkUtil;

/**
 * <p>
 * SAFEPOINT
 * </p>
 * 
 * <p>
 * All threads in the JVM are stopped.
 * </p>
 * 
 * <p>
 * Referenences:
 * </p>
 * 
 * <ul>
 * <li><a href=
 * "http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/87ee5ee27509/src/share/vm/runtime/vm_operations.hpp">JDK8</a>.</li>
 * </ul>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 1652.991: RevokeBias                       [    2403          0             13    ]      [     0     0     2    29     0    ]  0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class SafepointEvent implements LogEvent {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_BULK_REVOKE_BIAS + "|" + JdkRegEx.TRIGGER_DEOPTIMIZE
            + "|" + JdkRegEx.TRIGGER_REVOKE_BIAS + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " " + TRIGGER + "[ ]{1,23}" + JdkRegEx.THREAD_BLOCK
            + "[ ]{6}" + JdkRegEx.TIMES_BLOCK + "[ ]{2}" + JdkRegEx.NUMBER + "[ ]*$";

    private static Pattern pattern = Pattern.compile(REGEX);

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
     * The time for spinning threads to reach safepoint in milliseconds.
     */
    int timeSpin;

    /**
     * The time for blocked threads to reach safepoint in milliseconds.
     */
    int timeBlock;

    /**
     * The time for all threads to reach safepoint (sync) in milliseconds.
     */
    int timeSync;

    /**
     * The time for cleanup activities in milliseconds.
     */
    int timeCleanup;

    /**
     * The time for the safepoint activity (vmop) in milliseconds.
     */
    int timeVmop;

    /**
     * The page trap count.
     */
    int pageTrapCount;

    /**
     * The trigger for the safepoint event.
     */
    private String trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public SafepointEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            trigger = matcher.group(13);
            threadsTotal = Integer.parseInt(matcher.group(14));
            threadsSpinning = Integer.parseInt(matcher.group(15));
            threadsBlocked = Integer.parseInt(matcher.group(16));
            timeSpin = Integer.parseInt(matcher.group(17));
            timeBlock = Integer.parseInt(matcher.group(18));
            timeSync = Integer.parseInt(matcher.group(19));
            timeCleanup = Integer.parseInt(matcher.group(20));
            timeVmop = Integer.parseInt(matcher.group(21));
            pageTrapCount = Integer.parseInt(matcher.group(22));
        }

    }

    /**
     * Alternate constructor. Create <code>RevokeBiasEvent</code> from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the event started in milliseconds after JVM startup.
     * @param timeSync
     *            The time for all threads to reach safepoint (sync) in milliseconds.
     * @param timeCleanup
     *            The time for cleanup activities in milliseconds.
     * @param timeVmop
     *            The time for the safepoint activity (vmop) in milliseconds.
     */
    public SafepointEvent(String logEntry, long timestamp, int timeSync, int timeCleanup, int timeVmop) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.timeSync = timeSync;
        this.timeCleanup = timeCleanup;
        this.timeVmop = timeVmop;
    }

    public String getName() {
        return JdkUtil.LogEventType.SAFEPOINT.toString();
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

    public int getDuration() {
        return timeSync + timeCleanup + timeVmop;
    }

    public String getTrigger() {
        return trigger;
    }

}
