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

import org.github.vmcat.domain.BlankLineEvent;
import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.domain.UnknownEvent;
import org.github.vmcat.domain.jdk.DeoptimizeEvent;
import org.github.vmcat.domain.jdk.HeaderEvent;
import org.github.vmcat.domain.jdk.RevokeBiasEvent;
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
        BLANK_LINE, DEOPTIMIZE, HEADER, REVOKE_BIAS, TAG_BLOB, TAG_BLOB_SECT, TAG_DEPENDENCY_FAILED, TAG_HOTSPOT_LOG,
        //
        TAG_TTY, TAG_VM_ARGUMENTS, TAG_VM_ARGUMENTS_ARGS, TAG_VM_ARGUMENTS_COMMAND, TAG_VM_ARGUMENTS_LAUNCHER,
        //
        TAG_VM_ARGUMENTS_PROPERTIES, TAG_VM_VERSION, TAG_VM_VERSION_INFO, TAG_VM_VERSION_NAME, TAG_VM_VERSION_RELEASE,
        //
        TAG_WRITER, TAG_XML, UNKNOWN
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
        case DEOPTIMIZE:
            event = new DeoptimizeEvent(logLine);
            break;
        case HEADER:
            event = new HeaderEvent(logLine);
            break;
        case REVOKE_BIAS:
            event = new RevokeBiasEvent(logLine);
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
        if (DeoptimizeEvent.match(logLine))
            return LogEventType.DEOPTIMIZE;
        if (HeaderEvent.match(logLine))
            return LogEventType.HEADER;
        if (RevokeBiasEvent.match(logLine))
            return LogEventType.REVOKE_BIAS;
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
        case DEOPTIMIZE:
            event = new DeoptimizeEvent(logEntry);
            break;
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
}
