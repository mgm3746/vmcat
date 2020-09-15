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
package org.github.vmcat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.github.vmcat.Main;
import org.github.vmcat.domain.Jvm;
import org.github.vmcat.domain.JvmRun;
import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.domain.TagEvent;
import org.github.vmcat.domain.UnknownEvent;
import org.github.vmcat.domain.jdk.SafepointEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsArgsEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsCommandEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsLauncherEvent;
import org.github.vmcat.domain.jdk.TagVmArgumentsPropertiesEvent;
import org.github.vmcat.domain.jdk.TagVmVersionInfoEvent;
import org.github.vmcat.domain.jdk.TagVmVersionNameEvent;
import org.github.vmcat.domain.jdk.TagVmVersionReleaseEvent;
import org.github.vmcat.hsql.JvmDao;
import org.github.vmcat.util.VmUtil;
import org.github.vmcat.util.jdk.JdkUtil;

/**
 * <p>
 * Provides vm log analysis services to other layers.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Manager {

    /**
     * The JVM data access object.
     */
    private JvmDao jvmDao;

    /**
     * Default constructor.
     */
    public Manager() {
        this.jvmDao = new JvmDao();
    }

    /**
     * Parse the vm logging for the JVM run and store the data in the data store.
     * 
     * @param logFile
     *            The vm log file.
     */
    public void store(File logFile) {

        if (logFile == null) {
            return;
        }

        // Parse vm log file
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(logFile));
            String logLine = bufferedReader.readLine();
            while (logLine != null) {
                LogEvent event = JdkUtil.parseLogLine(logLine);
                if (event instanceof SafepointEvent) {
                    jvmDao.addSafepointEvent((SafepointEvent) event);
                } else if (event instanceof TagEvent) {
                    if ((event instanceof TagVmVersionNameEvent || event instanceof TagVmVersionReleaseEvent
                            || event instanceof TagVmArgumentsCommandEvent
                            || event instanceof TagVmArgumentsLauncherEvent)
                            && VmUtil.isHtmlEventStartTag(event.getLogEntry())) {
                        // flush data
                        logLine = bufferedReader.readLine();
                    } else if (event instanceof TagVmVersionInfoEvent
                            && VmUtil.isHtmlEventStartTag(event.getLogEntry())) {
                        jvmDao.setVersion(bufferedReader.readLine());
                    } else if (event instanceof TagVmArgumentsArgsEvent
                            && VmUtil.isHtmlEventStartTag(event.getLogEntry())) {
                        jvmDao.setOptions(bufferedReader.readLine());
                    } else if (event instanceof TagVmArgumentsPropertiesEvent
                            && VmUtil.isHtmlEventStartTag(event.getLogEntry())) {
                        boolean flushProperties = true;
                        while (flushProperties) {
                            LogEvent nextEvent = JdkUtil.parseLogLine(bufferedReader.readLine());
                            if (nextEvent instanceof TagVmArgumentsPropertiesEvent) {
                                // reached properties end tag
                                flushProperties = false;
                            }
                        }
                    }
                } else if (event instanceof UnknownEvent) {
                    if (jvmDao.getUnidentifiedLogLines().size() < Main.REJECT_LIMIT) {
                        jvmDao.getUnidentifiedLogLines().add(logLine);
                    }
                }

                // Populate events list.
                List<JdkUtil.LogEventType> eventTypes = jvmDao.getEventTypes();
                JdkUtil.LogEventType eventType = JdkUtil.determineEventType(event.getName());
                if (!eventTypes.contains(eventType)) {
                    eventTypes.add(eventType);
                }

                logLine = bufferedReader.readLine();
            }

            // Process final batch
            jvmDao.processSafepointBatch();

        } catch (

        FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Determine <code>SafepointEvent</code>s where throughput since last event does not meet the throughput goal.
     * 
     * @param jvm
     *            The JVM environment information.
     * @param throughputThreshold
     *            The bottleneck reporting throughput threshold.
     * @return A <code>List</code> of <code>SafepointEvent</code>s where the throughput between events is less than the
     *         throughput threshold goal.
     */
    private List<String> getBottlenecks(Jvm jvm, int throughputThreshold) {
        ArrayList<String> bottlenecks = new ArrayList<String>();
        List<SafepointEvent> safepointEvents = jvmDao.getSafepointEvents();
        Iterator<SafepointEvent> iterator = safepointEvents.iterator();
        SafepointEvent priorEvent = null;
        while (iterator.hasNext()) {
            SafepointEvent event = iterator.next();
            if (priorEvent != null && JdkUtil.isBottleneck(event, priorEvent, throughputThreshold)) {
                if (bottlenecks.size() == 0) {
                    // Add current and prior event
                    if (jvm.getStartDate() != null) {
                        // Convert timestamps to date/time
                        bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                jvm.getStartDate()));
                        bottlenecks.add(
                                JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvm.getStartDate()));
                    } else {
                        bottlenecks.add(priorEvent.getLogEntry());
                        bottlenecks.add(event.getLogEntry());
                    }
                } else {
                    if (jvm.getStartDate() != null) {
                        // Compare datetime, since bottleneck has datetime
                        if (!JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvm.getStartDate())
                                .equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                    jvm.getStartDate()));
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(),
                                    jvm.getStartDate()));
                        } else {
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(),
                                    jvm.getStartDate()));
                        }
                    } else {
                        // Compare timestamps, since bottleneck has timestamp
                        if (!priorEvent.getLogEntry().equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(priorEvent.getLogEntry());
                            bottlenecks.add(event.getLogEntry());
                        } else {
                            bottlenecks.add(event.getLogEntry());
                        }
                    }
                }
            }
            priorEvent = event;
        }
        return bottlenecks;
    }

    /**
     * Get JVM run data.
     * 
     * @param jvm
     *            JVM environment information.
     * @param throughputThreshold
     *            The throughput threshold for bottleneck reporting.
     * @return The JVM run data.
     */
    public JvmRun getJvmRun(Jvm jvm, int throughputThreshold) {
        JvmRun jvmRun = new JvmRun(jvm);
        jvmRun.setThroughputThreshold(throughputThreshold);
        jvmRun.setAnalysis(jvmDao.getAnalysis());
        jvmRun.setBottlenecks(getBottlenecks(jvm, throughputThreshold));
        jvmRun.setEventTypes(jvmDao.getEventTypes());
        jvmRun.setFirstSafepointEvent(jvmDao.getFirstSafepointEvent());
        jvmRun.setLastSafepointEvent(jvmDao.getLastSafepointEvent());
        jvmRun.setMaxPause(jvmDao.getMaxPause());
        jvmRun.setSafepointEventCount(jvmDao.getSafepointEventCount());
        jvmRun.setSafepointTotalPause(jvmDao.getSafepointTotalPause());
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.getJvm().setVersion(jvmDao.getVersion());
        jvmRun.getJvm().setOptions(jvmDao.getOptions());
        jvmRun.doAnalysis();
        return jvmRun;
    }
}