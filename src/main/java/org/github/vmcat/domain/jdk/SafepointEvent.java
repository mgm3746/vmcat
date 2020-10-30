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
import org.github.vmcat.util.jdk.Trigger;
import org.github.vmcat.util.jdk.Trigger.TriggerType;

/**
 * <p>
 * SAFEPOINT
 * </p>
 * 
 * <p>
 * All threads in the JVM are stopped.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * JDK8:
 * </p>
 * 
 * <pre>
 * 1652.991: RevokeBias                       [    2403          0             13    ]      [     0     0     2    29     0    ]  0
 * </pre>
 * 
 * <p>
 * JDK11:
 * </p>
 * 
 * <pre>
 *    0.562: GenCollectForAllocation         [                 9                 0             0 ][             0       0       0       0       0 ]               0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class SafepointEvent implements LogEvent {

    /**
     * Trigger(s) regular expression(s).
     */
    public static final String TRIGGER = "(" + Trigger.BULK_REVOKE_BIAS + "|" + Trigger.CMS_FINAL_REMARK + "|"
            + Trigger.CMS_INITIAL_MARK + "|" + Trigger.COLLECT_FOR_METADATA_ALLOCATION + "|" + Trigger.DEOPTIMIZE + "|"
            + Trigger.ENABLE_BIASED_LOCKING + "|" + Trigger.EXIT + "|" + Trigger.FIND_DEADLOCKS + "|"
            + Trigger.FORCE_SAFEPOINT + "|" + Trigger.FORCE_SAFEPOINT + "|" + Trigger.G1_COLLECT_FOR_ALLOCATION + "|"
            + Trigger.G1_INC_COLLECTION_PAUSE + "|" + Trigger.CGC_OPERATION + "|" + Trigger.GEN_COLLECT_FOR_ALLOCATION
            + "|" + Trigger.NO_VM_OPERATION + "|" + Trigger.PARALLEL_GC_FAILED_ALLOCATION + "|"
            + Trigger.PARALLEL_GC_SYSTEM_GC + "|" + Trigger.PRINT_JNI + "|" + Trigger.PRINT_THREADS + "|"
            + Trigger.REVOKE_BIAS + "|" + Trigger.SHENANDOAH_DEGENERATED_GC + "|"
            + Trigger.SHENANDOAH_FINAL_MARK_START_EVAC + "|" + Trigger.SHENANDOAH_FINAL_UPDATE_REFS + "|"
            + Trigger.SHENANDOAH_INIT_MARK + "|" + Trigger.SHENANDOAH_INIT_UPDATE_REFS + "|" + Trigger.THREAD_DUMP
            + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^[ ]{0,3}" + JdkRegEx.DECORATOR + " " + TRIGGER + "[ ]{1,29}"
            + JdkRegEx.THREAD_BLOCK + "[ ]{0,6}" + JdkRegEx.TIMES_BLOCK + "[ ]{2,15}" + JdkRegEx.NUMBER + "[ ]*$";

    private static Pattern pattern = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the VM event started in milliseconds after JVM startup.
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
     * The <code>TriggerType</code> for the safepoint event.
     */
    private TriggerType triggerType;

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
            String trigger = matcher.group(13);
            if (trigger.equals(Trigger.BULK_REVOKE_BIAS)) {
                triggerType = Trigger.TriggerType.BULK_REVOKE_BIAS;
            } else if (trigger.equals(Trigger.CMS_FINAL_REMARK)) {
                triggerType = Trigger.TriggerType.CMS_FINAL_REMARK;
            } else if (trigger.equals(Trigger.CMS_INITIAL_MARK)) {
                triggerType = Trigger.TriggerType.CMS_INITIAL_MARK;
            } else if (trigger.equals(Trigger.COLLECT_FOR_METADATA_ALLOCATION)) {
                triggerType = Trigger.TriggerType.COLLECT_FOR_METADATA_ALLOCATION;
            } else if (trigger.equals(Trigger.DEOPTIMIZE)) {
                triggerType = Trigger.TriggerType.DEOPTIMIZE;
            } else if (trigger.equals(Trigger.ENABLE_BIASED_LOCKING)) {
                triggerType = Trigger.TriggerType.ENABLE_BIASED_LOCKING;
            } else if (trigger.equals(Trigger.EXIT)) {
                triggerType = Trigger.TriggerType.EXIT;
            } else if (trigger.equals(Trigger.FIND_DEADLOCKS)) {
                triggerType = Trigger.TriggerType.FIND_DEADLOCKS;
            } else if (trigger.equals(Trigger.G1_COLLECT_FOR_ALLOCATION)) {
                triggerType = Trigger.TriggerType.G1_COLLECT_FOR_ALLOCATION;
            } else if (trigger.equals(Trigger.FORCE_SAFEPOINT)) {
                triggerType = Trigger.TriggerType.FORCE_SAFEPOINT;
            } else if (trigger.equals(Trigger.G1_INC_COLLECTION_PAUSE)) {
                triggerType = Trigger.TriggerType.G1_INC_COLLECTION_PAUSE;
            } else if (trigger.equals(Trigger.CGC_OPERATION)) {
                triggerType = Trigger.TriggerType.CGC_OPERATION;
            } else if (trigger.equals(Trigger.GEN_COLLECT_FOR_ALLOCATION)) {
                triggerType = Trigger.TriggerType.GEN_COLLECT_FOR_ALLOCATION;
            } else if (trigger.equals(Trigger.NO_VM_OPERATION)) {
                triggerType = Trigger.TriggerType.NO_VM_OPERATION;
            } else if (trigger.equals(Trigger.PARALLEL_GC_FAILED_ALLOCATION)) {
                triggerType = Trigger.TriggerType.PARALLEL_GC_FAILED_ALLOCATION;
            } else if (trigger.equals(Trigger.PARALLEL_GC_SYSTEM_GC)) {
                triggerType = Trigger.TriggerType.PARALLEL_GC_SYSTEM_GC;
            } else if (trigger.equals(Trigger.PRINT_JNI)) {
                triggerType = Trigger.TriggerType.PRINT_JNI;
            } else if (trigger.equals(Trigger.PRINT_THREADS)) {
                triggerType = Trigger.TriggerType.PRINT_THREADS;
            } else if (trigger.equals(Trigger.REVOKE_BIAS)) {
                triggerType = Trigger.TriggerType.REVOKE_BIAS;
            } else if (trigger.equals(Trigger.SHENANDOAH_DEGENERATED_GC)) {
                triggerType = Trigger.TriggerType.SHENANDOAH_DEGENERATED_GC;
            } else if (trigger.equals(Trigger.SHENANDOAH_FINAL_MARK_START_EVAC)) {
                triggerType = Trigger.TriggerType.SHENANDOAH_FINAL_MARK_START_EVAC;
            } else if (trigger.equals(Trigger.SHENANDOAH_FINAL_UPDATE_REFS)) {
                triggerType = Trigger.TriggerType.SHENANDOAH_FINAL_UPDATE_REFS;
            } else if (trigger.equals(Trigger.SHENANDOAH_INIT_MARK)) {
                triggerType = Trigger.TriggerType.SHENANDOAH_INIT_MARK;
            } else if (trigger.equals(Trigger.SHENANDOAH_INIT_UPDATE_REFS)) {
                triggerType = Trigger.TriggerType.SHENANDOAH_INIT_UPDATE_REFS;
            } else if (trigger.equals(Trigger.THREAD_DUMP)) {
                triggerType = Trigger.TriggerType.THREAD_DUMP;
            }
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

    public TriggerType getTriggerType() {
        return triggerType;
    }

}
