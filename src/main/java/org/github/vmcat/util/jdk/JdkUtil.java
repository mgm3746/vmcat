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
import org.github.vmcat.domain.jdk.FooterStatsEvent;
import org.github.vmcat.domain.jdk.HeaderEvent;
import org.github.vmcat.domain.jdk.SafepointEvent;
import org.github.vmcat.domain.jdk.TagArgsEvent;
import org.github.vmcat.domain.jdk.TagBlobEvent;
import org.github.vmcat.domain.jdk.TagCommandEvent;
import org.github.vmcat.domain.jdk.TagDependencyFailedEvent;
import org.github.vmcat.domain.jdk.TagDestroyVmEvent;
import org.github.vmcat.domain.jdk.TagHotspotLogEvent;
import org.github.vmcat.domain.jdk.TagInfoEvent;
import org.github.vmcat.domain.jdk.TagLauncherEvent;
import org.github.vmcat.domain.jdk.TagNameEvent;
import org.github.vmcat.domain.jdk.TagPropertiesEvent;
import org.github.vmcat.domain.jdk.TagReleaseEvent;
import org.github.vmcat.domain.jdk.TagSectEvent;
import org.github.vmcat.domain.jdk.TagTtyDoneEvent;
import org.github.vmcat.domain.jdk.TagTtyEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsEvent;
import org.github.vmcat.domain.jdk.TagVmVersionEvent;
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
        BLANK_LINE, FOOTER_STATS, HEADER, SAFEPOINT, TAG_ARGS, TAG_BLOB, TAG_COMMAND, TAG_DEPENDENCY_FAILED,
        //
        TAG_DESTROY_VM, TAG_HOTSPOT_LOG, TAG_INFO, TAG_LAUNCHER, TAG_NAME, TAG_PROPERTIES, TAG_RELEASE, TAG_SECT,
        //
        TAG_TTY, TAG_TTY_DONE, TAG_VM_ARGUMENTS, TAG_VM_VERSION, TAG_WRITER, TAG_XML, UNKNOWN
    };

    /**
     * Create <code>LogEvent</code> from VM log line.
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
        case FOOTER_STATS:
            event = new FooterStatsEvent(logLine);
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
        case TAG_SECT:
            event = new TagSectEvent(logLine);
            break;
        case TAG_DEPENDENCY_FAILED:
            event = new TagDependencyFailedEvent(logLine);
            break;
        case TAG_DESTROY_VM:
            event = new TagDestroyVmEvent(logLine);
            break;
        case TAG_HOTSPOT_LOG:
            event = new TagHotspotLogEvent(logLine);
            break;
        case TAG_TTY:
            event = new TagTtyEvent(logLine);
            break;
        case TAG_TTY_DONE:
            event = new TagTtyDoneEvent(logLine);
            break;
        case TAG_VM_ARGUMENTS:
            event = new TagVmArgumentsEvent(logLine);
            break;
        case TAG_ARGS:
            event = new TagArgsEvent(logLine);
            break;
        case TAG_COMMAND:
            event = new TagCommandEvent(logLine);
            break;
        case TAG_LAUNCHER:
            event = new TagLauncherEvent(logLine);
            break;
        case TAG_PROPERTIES:
            event = new TagPropertiesEvent(logLine);
            break;
        case TAG_VM_VERSION:
            event = new TagVmVersionEvent(logLine);
            break;
        case TAG_INFO:
            event = new TagInfoEvent(logLine);
            break;
        case TAG_NAME:
            event = new TagNameEvent(logLine);
            break;
        case TAG_RELEASE:
            event = new TagReleaseEvent(logLine);
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
        if (FooterStatsEvent.match(logLine))
            return LogEventType.FOOTER_STATS;
        if (HeaderEvent.match(logLine))
            return LogEventType.HEADER;
        if (SafepointEvent.match(logLine))
            return LogEventType.SAFEPOINT;
        if (TagBlobEvent.match(logLine))
            return LogEventType.TAG_BLOB;
        if (TagSectEvent.match(logLine))
            return LogEventType.TAG_SECT;
        if (TagDependencyFailedEvent.match(logLine))
            return LogEventType.TAG_DEPENDENCY_FAILED;
        if (TagDestroyVmEvent.match(logLine))
            return LogEventType.TAG_DESTROY_VM;
        if (TagHotspotLogEvent.match(logLine))
            return LogEventType.TAG_HOTSPOT_LOG;
        if (TagTtyEvent.match(logLine))
            return LogEventType.TAG_TTY;
        if (TagTtyDoneEvent.match(logLine))
            return LogEventType.TAG_TTY_DONE;
        if (TagVmArgumentsEvent.match(logLine))
            return LogEventType.TAG_VM_ARGUMENTS;
        if (TagArgsEvent.match(logLine))
            return LogEventType.TAG_ARGS;
        if (TagCommandEvent.match(logLine))
            return LogEventType.TAG_COMMAND;
        if (TagLauncherEvent.match(logLine))
            return LogEventType.TAG_LAUNCHER;
        if (TagPropertiesEvent.match(logLine))
            return LogEventType.TAG_PROPERTIES;
        if (TagVmVersionEvent.match(logLine))
            return LogEventType.TAG_VM_VERSION;
        if (TagInfoEvent.match(logLine))
            return LogEventType.TAG_INFO;
        if (TagNameEvent.match(logLine))
            return LogEventType.TAG_NAME;
        if (TagReleaseEvent.match(logLine))
            return LogEventType.TAG_RELEASE;
        if (TagWriterEvent.match(logLine))
            return LogEventType.TAG_WRITER;
        if (TagXmlEvent.match(logLine))
            return LogEventType.TAG_XML;

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
        case FOOTER_STATS:
        case HEADER:
        case TAG_ARGS:
        case TAG_BLOB:
        case TAG_COMMAND:
        case TAG_DEPENDENCY_FAILED:
        case TAG_DESTROY_VM:
        case TAG_HOTSPOT_LOG:
        case TAG_INFO:
        case TAG_LAUNCHER:
        case TAG_NAME:
        case TAG_PROPERTIES:
        case TAG_RELEASE:
        case TAG_SECT:
        case TAG_TTY:
        case TAG_TTY_DONE:
        case TAG_VM_ARGUMENTS:
        case TAG_VM_VERSION:
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
     * Determine if the <code>SafepointEvent</code> should be classified as a bottleneck.
     * 
     * @param currentSafepointEvent
     *            Current <code>SafepointEvent</code>.
     * @param previouseSafepointEvent
     *            Previous <code>SafepointEvent</code>.
     * @param throughputThreshold
     *            Throughput threshold (percent of time not in safepoint for a given time interval) to be considered a
     *            bottleneck. Whole number 0-100.
     * @return True if the <code>SafepointEvent</code> pause time meets the bottleneck definition.
     */
    public static final boolean isBottleneck(SafepointEvent currentSafepointEvent,
            SafepointEvent previouseSafepointEvent, int throughputThreshold) throws TimeWarpException {
        /*
         * Check for logging time warps, which could be an indication of mixed logging from multiple JVM runs. JDK8
         * seems to have threading issues where sometimes logging gets mixed up under heavy load, and an event appears
         * to start before the previous event finished. They are mainly very small overlaps or a few milliseconds.
         */
        if (currentSafepointEvent.getTimestamp() < previouseSafepointEvent.getTimestamp()) {
            throw new TimeWarpException("Bad order: " + Constants.LINE_SEPARATOR + previouseSafepointEvent.getLogEntry()
                    + Constants.LINE_SEPARATOR + currentSafepointEvent.getLogEntry());
        } else if (currentSafepointEvent.getTimestamp() < (previouseSafepointEvent.getTimestamp()
                + previouseSafepointEvent.getDuration() - 1000)) {
            // Only report if overlap > 1 sec to account for small overlaps due to JDK
            // threading issues
            throw new TimeWarpException(
                    "Event overlap: " + Constants.LINE_SEPARATOR + previouseSafepointEvent.getLogEntry()
                            + Constants.LINE_SEPARATOR + currentSafepointEvent.getLogEntry());
        } else if (currentSafepointEvent
                .getTimestamp() <= (previouseSafepointEvent.getTimestamp() + previouseSafepointEvent.getDuration())) {
            // Small (<1 sec) event overlap
            return true;
        } else {
            /*
             * Timestamp is the start of a vm event; therefore, the interval is from the end of the prior event to the
             * end of the current event.
             */
            long interval = currentSafepointEvent.getTimestamp() + currentSafepointEvent.getDuration()
                    - previouseSafepointEvent.getTimestamp() - previouseSafepointEvent.getDuration();
            // Determine the maximum duration for the given interval that meets the
            // throughput goal.
            BigDecimal durationThreshold = new BigDecimal(100 - throughputThreshold);
            durationThreshold = durationThreshold.movePointLeft(2);
            durationThreshold = durationThreshold.multiply(new BigDecimal(interval));
            durationThreshold.setScale(0, RoundingMode.DOWN);
            return (currentSafepointEvent.getDuration() > durationThreshold.intValue());
        }
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
        // Add the colon or space after the timestamp format so durations will not get
        // picked up.
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
