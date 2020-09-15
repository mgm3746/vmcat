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

import java.util.Calendar;

import org.github.vmcat.domain.TimeWarpException;
import org.github.vmcat.domain.jdk.SafepointEvent;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkUtil extends TestCase {

    public void testConvertLogEntryTimestampsToDate() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        String logLine = "20.189: RevokeBias                       [      24          0              2    ]      "
                + "[     0     0     0     0     0    ]  0";
        String logLineConverted = "1966-08-18 19:22:04,201: RevokeBias                       "
                + "[      24          0              2    ]      [     0     0     0     0     0    ]  0";
        Assert.assertEquals("Timestamps not converted to date/time correctly", logLineConverted,
                JdkUtil.convertLogEntryTimestampsToDateStamp(logLine, calendar.getTime()));
    }

    public void testBottleneckDetectionWholeNumbers() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int timeSync1 = 100;
        int timeCleanup1 = 50;
        int timeVmop1 = 350;
        SafepointEvent priorEvent = new SafepointEvent(logLine1, timestamp1, timeSync1, timeCleanup1, timeVmop1);

        // 1 second between GCs with duration of .5 seconds
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int timeSync2 = 100;
        int timeCleanup2 = 50;
        int timeVmop2 = 350;
        SafepointEvent currentEvent = new SafepointEvent(logLine2, timestamp2, timeSync2, timeCleanup2, timeVmop2);

        // Test boundary
        int throughputThreshold = 50;
        Assert.assertFalse("Event incorrectly flagged as a bottleneck.",
                JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));

        // Test bottleneck
        timeVmop2 = 351;
        currentEvent = new SafepointEvent(logLine2, timestamp2, timeSync2, timeCleanup2, timeVmop2);
        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));
    }

    public void testBottleneckDetectionFractions() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int timeSync1 = 10;
        int timeCleanup1 = 20;
        int timeVmop1 = 70;
        SafepointEvent priorEvent = new SafepointEvent(logLine1, timestamp1, timeSync1, timeCleanup1, timeVmop1);

        // 123 ms between GCs with duration of 33 ms
        String logLine2 = "test2";
        long timestamp2 = 10123L;
        int timeSync2 = 1;
        int timeCleanup2 = 2;
        int timeVmop2 = 30;
        SafepointEvent currentEvent = new SafepointEvent(logLine2, timestamp2, timeSync2, timeCleanup2, timeVmop2);

        // Test boundary
        int throughputThreshold = 41;
        Assert.assertFalse("Event incorrectly flagged as a bottleneck.",
                JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));

        // Test boundary
        throughputThreshold = 42;
        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));
    }

    public void testTimeWarp() {
        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int timeSync1 = 10;
        int timeCleanup1 = 20;
        int timeVmop1 = 70;
        SafepointEvent priorEvent = new SafepointEvent(logLine1, timestamp1, timeSync1, timeCleanup1, timeVmop1);

        // 2nd event starts immediately after the first
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int timeSync2 = 100;
        int timeCleanup2 = 200;
        int timeVmop2 = 300;
        SafepointEvent currentEvent = new SafepointEvent(logLine2, timestamp2, timeSync2, timeCleanup2, timeVmop2);

        // Test boundary
        int throughputThreshold = 100;

        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));

        // Decrease timestamp by 1 ms to 2nd event start before 1st event finishes
        timestamp2 = 10999L;
        currentEvent = new SafepointEvent(logLine2, timestamp2, timeSync2, timeCleanup2, timeVmop2);
        try {
            Assert.assertTrue("Event should have been flagged as a bottleneck.",
                    JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));
        } catch (Exception e) {
            Assert.assertTrue("Expected TimeWarpException not thrown.", e instanceof TimeWarpException);
        }
    }

    public void testTimeWarpLoggingReverseOrder() {
        String previousLogLine = "10.222: RevokeBias                       [      24          1              1    ]"
                + "      [     0     0     0     0     0    ]  0";
        SafepointEvent priorEvent = new SafepointEvent(previousLogLine);

        // 2nd event starts before first
        String logLine = "9.406: RevokeBias                       [      22          0              1    ]      "
                + "[     0     0     0     0     0    ]  0";
        SafepointEvent currentEvent = new SafepointEvent(logLine);

        // Test boundary
        int throughputThreshold = 100;

        try {
            Assert.assertTrue("Event should have been flagged as a bottleneck.",
                    JdkUtil.isBottleneck(currentEvent, priorEvent, throughputThreshold));
        } catch (Exception e) {
            Assert.assertTrue("Expected TimeWarpException not thrown.", e instanceof TimeWarpException);
        }
    }
}
