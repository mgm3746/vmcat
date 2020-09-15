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

import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestSafepointEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        Assert.assertTrue(JdkUtil.TriggerType.RevokeBias.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof SafepointEvent);
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SAFEPOINT;
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        long timestamp = 1665730;
        int timeSync = 10;
        int timeClean = 30;
        int timeVmop = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SAFEPOINT.toString() + " not parsed.",
                JdkUtil.hydrateSafepointEvent(eventType, logLine, timestamp, timeSync, timeClean,
                        timeVmop) instanceof SafepointEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SAFEPOINT.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SAFEPOINT));
    }

    public void testLogLineTriggerRevokeBias() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1665730, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.RevokeBias.toString(),
                event.getTrigger());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2409,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 2,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 74,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 3, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 2,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 10,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 30, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 0, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 1, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 40, event.getDuration());
    }

    public void testLogLineWhiteSpaceAtEnd() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
    }

    public void testLogLineTriggerDeoptimize() {
        String logLine = "1127.742: Deoptimize                       [    2531          0             38    ]      "
                + "[     0     5     7    30    34    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1127742, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.Deoptimize.toString(),
                event.getTrigger());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2531,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 38,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 5,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 7,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 30, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 34, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 71, event.getDuration());
    }
}
