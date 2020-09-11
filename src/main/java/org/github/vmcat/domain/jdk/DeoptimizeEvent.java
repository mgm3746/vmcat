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

import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.util.jdk.JdkMath;
import org.github.vmcat.util.jdk.JdkRegEx;
import org.github.vmcat.util.jdk.JdkUtil;

/**
 * <p>
 * DEOPTIMIZE
 * </p>
 * 
 * <p>
 * When the compiler has to recompile previously compiled code due to the compiled code no longer being valid (e.g. a
 * dynamic object has changed) or with tiered compilation when client compiled codes is replaced with server complied
 * code.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 4.283: Deoptimize                       [      17          0              0    ]      [     0     0     0     0     0    ]  0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class DeoptimizeEvent implements SafepointEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " Deoptimize[ ]{23}" + JdkRegEx.THREAD_BLOCK
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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public DeoptimizeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            threadsTotal = Integer.parseInt(matcher.group(13));
            threadsSpinning = Integer.parseInt(matcher.group(14));
            threadsBlocked = Integer.parseInt(matcher.group(15));
            timeSpin = Integer.parseInt(matcher.group(16));
            timeBlock = Integer.parseInt(matcher.group(17));
            timeSync = Integer.parseInt(matcher.group(18));
            timeCleanup = Integer.parseInt(matcher.group(19));
            timeVmop = Integer.parseInt(matcher.group(20));
            pageTrapCount = Integer.parseInt(matcher.group(21));
        }

    }

    public String getName() {
        return JdkUtil.LogEventType.DEOPTIMIZE.toString();
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

}
