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
package org.github.vmcat.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.github.vmcat.domain.jdk.SafepointEvent;
import org.github.vmcat.domain.jdk.SafepointEventSummary;
import org.github.vmcat.util.Constants;
import org.github.vmcat.util.VmUtil;
import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;

/**
 * JVM run data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmRun {

    /**
     * JVM environment information.
     */
    private Jvm jvm;

    /**
     * Minimum throughput (percent of time spent not in safepoint for a given time interval) to not be flagged a
     * bottleneck.
     */
    private int throughputThreshold;

    /**
     * Total number of SafepointEvent events.
     */
    private long safepointEventCount;

    /**
     * Total SafePointEvent pause duration (milliseconds).
     */
    private long safepointTotalPause;

    /**
     * The first <code>SafepointEvent</code>.
     */
    private SafepointEvent firstSafepointEvent;

    /**
     * The last <code>SafepointEvent</code>.
     */
    private SafepointEvent lastSafepointEvent;

    /**
     * <code>SafepointEvent</code>s where throughput does not meet the throughput goal.
     */
    private List<String> bottlenecks;

    /**
     * Log lines that do not match any existing logging patterns.
     */
    private List<String> unidentifiedLogLines;

    /**
     * Analysis.
     */
    private List<Analysis> analysis;

    /**
     * Event types.
     */
    private List<LogEventType> eventTypes;

    /**
     * <code>SafepointEventSummary</code> used for reporting.
     */
    private List<SafepointEventSummary> safepointEventSummaries;

    /**
     * Maximum pause duration (milliseconds).
     */
    private int maxPause;

    /**
     * Constructor accepting throughput threshold, JVM services, and JVM environment information.
     * 
     * @param jvm
     *            JVM environment information.
     */
    public JvmRun(Jvm jvm) {
        this.jvm = jvm;
    }

    public Jvm getJvm() {
        return jvm;
    }

    public void setJvm(Jvm jvm) {
        this.jvm = jvm;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public void setUnidentifiedLogLines(List<String> unidentifiedLogLines) {
        this.unidentifiedLogLines = unidentifiedLogLines;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<LogEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<SafepointEventSummary> getSafepointEventSummaries() {
        return safepointEventSummaries;
    }

    public void setSafepointEventSummaries(List<SafepointEventSummary> safepointEventSummaries) {
        this.safepointEventSummaries = safepointEventSummaries;
    }

    public List<String> getBottlenecks() {
        return bottlenecks;
    }

    public void setBottlenecks(List<String> bottlenecks) {
        this.bottlenecks = bottlenecks;
    }

    public int getThroughputThreshold() {
        return throughputThreshold;
    }

    public void setThroughputThreshold(int throughputThreshold) {
        this.throughputThreshold = throughputThreshold;
    }

    public long getSafepointEventCount() {
        return safepointEventCount;
    }

    public void setSafepointEventCount(long safepointEventCount) {
        this.safepointEventCount = safepointEventCount;
    }

    public long getSafepointTotalPause() {
        return safepointTotalPause;
    }

    public void setSafepointTotalPause(long safepointTotalPause) {
        this.safepointTotalPause = safepointTotalPause;
    }

    public SafepointEvent getFirstSafepointEvent() {
        return firstSafepointEvent;
    }

    public void setFirstSafepointEvent(SafepointEvent firstSafepointEvent) {
        this.firstSafepointEvent = firstSafepointEvent;
    }

    public SafepointEvent getLastSafepointEvent() {
        return lastSafepointEvent;
    }

    public void setLastSafepointEvent(SafepointEvent lastSafepointEvent) {
        this.lastSafepointEvent = lastSafepointEvent;
    }

    public int getMaxPause() {
        return maxPause;
    }

    public void setMaxPause(int maxPause) {
        this.maxPause = maxPause;
    }

    /**
     * Do analysis.
     */
    public void doAnalysis() {

        // Unidentified logging lines
        if (getUnidentifiedLogLines().size() > 0) {
            analysis.add(0, Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT);
        }

        if (jvm.getOptions() != null) {
            doJvmOptionsAnalysis();
        }

        // TODO:
        if (haveData()) {
            doDataAnalysis();
        }
    }

    /**
     * Do data analysis.
     */
    private void doDataAnalysis() {
        // Check for partial log
        if (this.firstSafepointEvent != null && VmUtil.isPartialLog(firstSafepointEvent.getTimestamp())) {
            analysis.add(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }
    }

    /**
     * @return true if there is data, false otherwise (e.g. no logging lines recognized).
     */
    public boolean haveData() {
        boolean haveData = true;
        // TODO:
        return haveData;
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // TODO:
    }

    /**
     * @return JVM run duration (milliseconds).
     */
    public long getJvmRunDuration() {

        long start = 0;
        if (getFirstSafepointEvent() != null
                && getFirstSafepointEvent().getTimestamp() > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
            // partial log
            start = getFirstSafepointEvent().getTimestamp();
        }

        long end = 0;
        long lastSafepointEventTimeStamp = 0;
        long lastSafepointEventDuration = 0;
        if (lastSafepointEvent != null) {
            lastSafepointEventTimeStamp = lastSafepointEvent.getTimestamp();
            lastSafepointEventDuration = lastSafepointEvent.getDuration();
        }
        end = lastSafepointEventTimeStamp + lastSafepointEventDuration;

        return end - start;
    }

    /**
     * @return Throughput based on <code>SafepointEvent</code>s as a percent rounded to the nearest integer. Throughput
     *         is the percent of time not in safepoint. 0 means all time was spent in safepoint. 100 means no time was
     *         spent in safepoint.
     */
    public long getThroughput() {
        long safepointThroughput;
        if (safepointEventCount > 0) {
            long timeNotSafepoint = getJvmRunDuration() - new Long(safepointTotalPause).longValue();
            BigDecimal throughput = new BigDecimal(timeNotSafepoint);
            throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, RoundingMode.HALF_EVEN);
            throughput = throughput.movePointRight(2);
            safepointThroughput = throughput.longValue();

        } else {
            safepointThroughput = 100L;
        }
        return safepointThroughput;
    }

}
