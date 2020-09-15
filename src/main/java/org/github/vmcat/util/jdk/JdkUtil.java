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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.github.vmcat.domain.BlankLineEvent;
import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.domain.TimeWarpException;
import org.github.vmcat.domain.UnknownEvent;
import org.github.vmcat.domain.jdk.HeaderEvent;
import org.github.vmcat.domain.jdk.SafepointEvent;
import org.github.vmcat.domain.jdk.TagBlobEvent;
import org.github.vmcat.domain.jdk.TagBlobSectEvent;
import org.github.vmcat.domain.jdk.TagDependencyFailedEvent;
import org.github.vmcat.domain.jdk.TagHotspotLogEvent;
import org.github.vmcat.domain.jdk.TagTtyEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsArgsEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsCommandEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsLauncherEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsPropertiesEvent;
import org.github.vmcat.domain.jdk.TagVmVersionEvent;
import org.github.vmcat.domain.jdk.TagVmVersionInfoEvent;
import org.github.vmcat.domain.jdk.TagVmVersionNameEvent;
import org.github.vmcat.domain.jdk.TagVmVersionReleaseEvent;
import org.github.vmcat.domain.jdk.TagWriterEvent;
import org.github.vmcat.domain.jdk.TagXmlEvent;
import org.github.vmcat.util.Constants;
import org.github.vmcat.util.VmUtil;

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
        BLANK_LINE, HEADER, SAFEPOINT, TAG_BLOB, TAG_BLOB_SECT, TAG_DEPENDENCY_FAILED, TAG_HOTSPOT_LOG, TAG_TTY,
        //
        TAG_VM_ARGUMENTS, TAG_VM_ARGUMENTS_ARGS, TAG_VM_ARGUMENTS_COMMAND, TAG_VM_ARGUMENTS_LAUNCHER,
        //
        TAG_VM_ARGUMENTS_PROPERTIES, TAG_VM_VERSION, TAG_VM_VERSION_INFO, TAG_VM_VERSION_NAME, TAG_VM_VERSION_RELEASE,
        //
        TAG_WRITER, TAG_XML, UNKNOWN
    };

    /**
     * Defined triggers.
     */
    public enum TriggerType {
        BULK_REVOKE_BIAS, COLLECT_FOR_METADATA_ALLOCATION, DEOPTIMIZE, ENABLE_BIASED_LOCKING, FIND_DEADLOCKS,
        //
        FORCE_SAFEPOINT, NO_VM_OPERATION, PARALLEL_GC_FAILED_ALLOCATION, PARALLEL_GC_SYSTEM_GC, PRINT_JNI,
        //
        PRINT_THREADS, REVOKE_BIAS, THREAD_DUMP, UNKNOWN
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

        case BLANK_LINE:
            event = new BlankLineEvent(logLine);
            break;
        case HEADER:
            event = new HeaderEvent(logLine);
            break;
        case SAFEPOINT:
            event = new SafepointEvent(logLine);
            break;
        case TAG_BLOB:
            event = new TagBlobEvent(logLine);
            break;
        case TAG_BLOB_SECT:
            event = new TagBlobSectEvent(logLine);
            break;
        case TAG_DEPENDENCY_FAILED:
            event = new TagDependencyFailedEvent(logLine);
            break;
        case TAG_HOTSPOT_LOG:
            event = new TagHotspotLogEvent(logLine);
            break;
        case TAG_TTY:
            event = new TagTtyEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS:
            event = new TagVmArgumentsEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS_ARGS:
            event = new TagVmArgumentsArgsEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS_COMMAND:
            event = new TagVmArgumentsCommandEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS_LAUNCHER:
            event = new TagVmArgumentsLauncherEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS_PROPERTIES:
            event = new TagVmArgumentsPropertiesEvent(logLine);
            break;
        case TAG_VM_VERSION:
            event = new TagVmVersionEvent(logLine);
            break;
        case TAG_VM_VERSION_INFO:
            event = new TagVmVersionInfoEvent(logLine);
            break;
        case TAG_VM_VERSION_NAME:
            event = new TagVmVersionNameEvent(logLine);
            break;
        case TAG_VM_VERSION_RELEASE:
            event = new TagVmVersionReleaseEvent(logLine);
            break;
        case TAG_WRITER:
            event = new TagWriterEvent(logLine);
            break;
        case TAG_XML:
            event = new TagXmlEvent(logLine);
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
        if (BlankLineEvent.match(logLine))
            return LogEventType.BLANK_LINE;
        if (HeaderEvent.match(logLine))
            return LogEventType.HEADER;
        if (SafepointEvent.match(logLine))
            return LogEventType.SAFEPOINT;
        if (TagBlobEvent.match(logLine))
            return LogEventType.TAG_BLOB;
        if (TagBlobSectEvent.match(logLine))
            return LogEventType.TAG_BLOB_SECT;
        if (TagDependencyFailedEvent.match(logLine))
            return LogEventType.TAG_DEPENDENCY_FAILED;
        if (TagHotspotLogEvent.match(logLine))
            return LogEventType.TAG_HOTSPOT_LOG;
        if (TagTtyEvent.match(logLine))
            return LogEventType.TAG_TTY;
        if (TagVmArgumentsEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS;
        if (TagVmArgumentsArgsEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS_ARGS;
        if (TagVmArgumentsCommandEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS_COMMAND;
        if (TagVmArgumentsLauncherEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS_LAUNCHER;
        if (TagVmArgumentsPropertiesEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS_PROPERTIES;
        if (TagVmVersionEvent.match(logLine))
            return LogEventType.TAG_VM_VERSION;
        if (TagVmVersionInfoEvent.match(logLine))
            return LogEventType.TAG_VM_VERSION_INFO;
        if (TagVmVersionNameEvent.match(logLine))
            return LogEventType.TAG_VM_VERSION_NAME;
        if (TagVmVersionReleaseEvent.match(logLine))
            return LogEventType.TAG_VM_VERSION_RELEASE;
        if (TagWriterEvent.match(logLine))
            return LogEventType.TAG_WRITER;
        if (TagXmlEvent.match(logLine))
            return LogEventType.TAG_XML;

        // no idea what event is
        return LogEventType.UNKNOWN;
    }

    /**
     * Identify the safepoint trigger.
     * 
     * @param trigger
     *            The trigger.
     * @return The <code>TriggerType</code>.
     */
    public static final TriggerType identifyTriggerType(String trigger) {
        if (TriggerType.BULK_REVOKE_BIAS.toString().matches(trigger))
            return TriggerType.BULK_REVOKE_BIAS;
        if (TriggerType.COLLECT_FOR_METADATA_ALLOCATION.toString().matches(trigger))
            return TriggerType.COLLECT_FOR_METADATA_ALLOCATION;
        if (TriggerType.DEOPTIMIZE.toString().matches(trigger))
            return TriggerType.DEOPTIMIZE;
        if (TriggerType.ENABLE_BIASED_LOCKING.toString().matches(trigger))
            return TriggerType.ENABLE_BIASED_LOCKING;
        if (TriggerType.FIND_DEADLOCKS.toString().matches(trigger))
            return TriggerType.FIND_DEADLOCKS;
        if (TriggerType.FORCE_SAFEPOINT.toString().matches(trigger))
            return TriggerType.FORCE_SAFEPOINT;
        if (TriggerType.NO_VM_OPERATION.toString().matches(trigger))
            return TriggerType.NO_VM_OPERATION;
        if (TriggerType.PARALLEL_GC_FAILED_ALLOCATION.toString().matches(trigger))
            return TriggerType.PARALLEL_GC_FAILED_ALLOCATION;
        if (TriggerType.PARALLEL_GC_SYSTEM_GC.toString().matches(trigger))
            return TriggerType.PARALLEL_GC_SYSTEM_GC;
        if (TriggerType.PRINT_JNI.toString().matches(trigger))
            return TriggerType.PRINT_JNI;
        if (TriggerType.PRINT_THREADS.toString().matches(trigger))
            return TriggerType.PRINT_THREADS;
        if (TriggerType.REVOKE_BIAS.toString().matches(trigger))
            return TriggerType.REVOKE_BIAS;
        if (TriggerType.THREAD_DUMP.toString().matches(trigger))
            return TriggerType.THREAD_DUMP;

        // no idea what trigger is
        return TriggerType.UNKNOWN;
    }

    /**
     * Create <code>SafepointEvent</code> from values.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @param logEntry
     *            Log entry.
     * @param timestamp
     *            Log entry timestamp.
     * @param timeSync
     *            The time for all threads to reach safepoint (sync) in milliseconds.
     * @param timeCleanup
     *            The time for cleanup activities in milliseconds.
     * @param timeVmop
     *            The time for the safepoint activity (vmop) in milliseconds.
     * @return The <code>SafepointEvent</code> for the given event values.
     */
    public static final SafepointEvent hydrateSafepointEvent(LogEventType eventType, String logEntry, long timestamp,
            int timeSync, int timeCleanup, int timeVmop) {
        return new SafepointEvent(logEntry, timestamp, timeSync, timeCleanup, timeVmop);
    }

    /**
     * @param eventType
     *            The event type to test.
     * @return true if the log event is should be included in the report event list, false otherwise.
     */
    public static final boolean isReportable(LogEventType eventType) {

        boolean reportable = true;

        switch (eventType) {
        case BLANK_LINE:
        case HEADER:
        case TAG_BLOB:
        case TAG_BLOB_SECT:
        case TAG_DEPENDENCY_FAILED:
        case TAG_HOTSPOT_LOG:
        case TAG_TTY:
        case TAG_VM_ARGUMENTS:
        case TAG_VM_ARGUMENTS_ARGS:
        case TAG_VM_ARGUMENTS_COMMAND:
        case TAG_VM_ARGUMENTS_LAUNCHER:
        case TAG_VM_ARGUMENTS_PROPERTIES:
        case TAG_VM_VERSION:
        case TAG_VM_VERSION_INFO:
        case TAG_VM_VERSION_NAME:
        case TAG_VM_VERSION_RELEASE:
        case TAG_WRITER:
        case TAG_XML:
        case UNKNOWN:
            reportable = false;
            break;
        default:
            break;
        }

        return reportable;
    }

    public static final LogEventType determineEventType(String eventTypeString) {
        LogEventType logEventType = null;
        LogEventType[] logEventTypes = LogEventType.values();
        for (int i = 0; i < logEventTypes.length; i++) {
            if (logEventTypes[i].toString().equals(eventTypeString)) {
                logEventType = logEventTypes[i];
                break;
            }
        }
        return logEventType;
    }

    /**
     * TriggerType Determine if the <code>SafepointEvent</code> should be classified as a bottleneck.
     * 
     * @param event
     *            Current <code>SafepointEvent</code>.
     * @param event
     *            Previous <code>SafepointEvent</code>.
     * @param throughputThreshold
     *            Throughput threshold (percent of time spent not is safepoint for a given time interval) to be
     *            considered a bottleneck. Whole number 0-100.
     * @return True if the <code>SafepointEvent</code> pause time meets the bottleneck definition.
     */
    public static final boolean isBottleneck(SafepointEvent safepointEvent, SafepointEvent event,
            int throughputThreshold) throws TimeWarpException {

        /*
         * Current event should not start until prior even finishes. Allow 1 thousandth of a second overlap to account,
         * DEOPTIMIZE, ENABLE_BIASED_LOCKING, REVOKE_BIAS for precision and rounding limitations.
         */
        if (safepointEvent.getTimestamp() < (event.getTimestamp() + event.getDuration() - 1)) {
            throw new TimeWarpException("Event overlap: " + Constants.LINE_SEPARATOR + event.getLogEntry()
                    + Constants.LINE_SEPARATOR + safepointEvent.getLogEntry());
        }

        /*
         * Timestamp is the start of a garbage collection event; therefore, the interval is from the end of the prior
         * event to the end of the current event.
         */
        long interval = safepointEvent.getTimestamp() + safepointEvent.getDuration() - event.getTimestamp()
                - event.getDuration();
        if (interval < 0) {
            throw new TimeWarpException("Negative interval: " + Constants.LINE_SEPARATOR + event.getLogEntry()
                    + Constants.LINE_SEPARATOR + safepointEvent.getLogEntry());
        }

        // Determine the maximum duration for the given interval that meets the
        // throughput goal.
        BigDecimal durationThreshold = new BigDecimal(100 - throughputThreshold);
        durationThreshold = durationThreshold.movePointLeft(2);
        durationThreshold = durationThreshold.multiply(new BigDecimal(interval));
        durationThreshold.setScale(0, RoundingMode.DOWN);
        return (safepointEvent.getDuration() > durationThreshold.intValue());
    }

    /**
     * Convert all log entry timestamps to a datestamp.
     * 
     * @param logEntry
     *            The log entry.
     * @param jvmStartDate
     *            The date/time the JVM started.
     * @return the log entry with the timestamp converted to a datestamp.
     */
    public static final String convertLogEntryTimestampsToDateStamp(String logEntry, Date jvmStartDate) {
        // Add the colon or space after the timestamp format so durations will not get picked up.
        Pattern pattern = Pattern.compile(JdkRegEx.TIMESTAMP + "(: )");
        Matcher matcher = pattern.matcher(logEntry);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            Date date = VmUtil.getDatePlusTimestamp(jvmStartDate,
                    JdkMath.convertSecsToMillis(matcher.group(1)).longValue());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            // Only update the timestamp, keep the colon or space.
            matcher.appendReplacement(sb, formatter.format(date) + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Check to see if a log line includes any datestamps.
     * 
     * @param logLine
     *            The log line.
     * @return True if the log line includes a datestamp, false otherwise..
     */
    public static final String getDateStamp(String logLine) {
        String datestamp = null;
        String regex = "^(.*)" + JdkRegEx.DATESTAMP + "(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find()) {
            datestamp = matcher.group(2);
        }
        return datestamp;
    }
}
