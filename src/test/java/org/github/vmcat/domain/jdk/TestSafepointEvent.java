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
        Assert.assertTrue(JdkUtil.TriggerType.REVOKE_BIAS.toString() + " not parsed.",
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
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.REVOKE_BIAS, event.getTriggerType());
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
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.DEOPTIMIZE, event.getTriggerType());
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

    public void testLogLineTriggerBulkRevokeBias() {
        String logLine = "1618.652: BulkRevokeBias                   [    2456          0              2    ]      "
                + "[     0     0     1    31   171    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1618652, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.BULK_REVOKE_BIAS,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2456,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 2,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 1,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 31, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 171, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 203, event.getDuration());
    }

    public void testLogLineTriggerEnableBiasLocking() {
        String logLine = "9.086: EnableBiasedLocking              [      22          0              2    ]      "
                + "[     0     0     0     0     1    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 9086, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.ENABLE_BIASED_LOCKING,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 22,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 2,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 0,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 0, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 1, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 1, event.getDuration());
    }

    public void testLogLineTriggerThreadDump() {
        String logLine = "236.953: ThreadDump                       [     908          0              3    ]      "
                + "[     0    13    13     6    70    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 236953, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.THREAD_DUMP, event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 908,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 3,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 13,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 13,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 6, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 70, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 89, event.getDuration());
    }

    public void testLogLineTriggerPrintThreads() {
        String logLine = "1149.958: PrintThreads                     [    2411          2             67    ]      "
                + "[     2     7    11    28  2841    ]  2";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1149958, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.PRINT_THREADS, event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2411,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 2,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 67,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 2, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 7,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 11,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 28, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 2841, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 2, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 2880, event.getDuration());
    }

    public void testLogLineTriggerCollectForMetadataAllocation() {
        String logLine = "11.304: CollectForMetadataAllocation       [      25          0              4    ]      "
                + "[     0     0     0     0  1837    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 11304, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.COLLECT_FOR_METADATA_ALLOCATION,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 25,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 4,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 0,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 0, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 1837, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 1837, event.getDuration());
    }

    public void testLogLineTriggerFindDeadlocks() {
        String logLine = "1659.252: FindDeadlocks                    [    2404          0              0    ]      "
                + "[     0     0     0    28     6    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1659252, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.FIND_DEADLOCKS,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2404,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 0,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 0,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 28, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 6, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 34, event.getDuration());
    }

    public void testLogLineTriggerPrintJni() {
        String logLine = "1659.220: PrintJNI                         [    2404          0              1    ]      "
                + "[     0     0     1    29     0    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1659220, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.PRINT_JNI, event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2404,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 1,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 1,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 29, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 0, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 30, event.getDuration());
    }

    public void testLogLineForceSafepoint() {
        String logLine = "678.484: ForceSafepoint                   [    2370          2            132    ]      "
                + "[    11    13    29    30     0    ]  1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 678484, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.FORCE_SAFEPOINT,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2370,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 2,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 132,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 11, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 13,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 29,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 30, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 0, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 1, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 59, event.getDuration());
    }

    public void testLogLineTriggerParallelGcFailedAllocation() {
        String logLine = "864.397: ParallelGCFailedAllocation       [    2379          3            139    ]      "
                + "[     7    18    27    28  1236    ]  1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 864397, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.PARALLEL_GC_FAILED_ALLOCATION,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 2379,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 3,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 139,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 7, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 18,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 27,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 28, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 1236, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 1, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 1291, event.getDuration());
    }

    public void testLogLineTriggerParallelGcSystemGc() {
        String logLine = "276.888: ParallelGCSystemGC               [    1565          0              9    ]      "
                + "[     0     0     1    14  1804    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 276888, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.PARALLEL_GC_SYSTEM_GC,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 1565,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 9,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 1,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 14, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 1804, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 1819, event.getDuration());
    }

    public void testLogLineTriggerGenCollectForAllocation() {
        String logLine = "47.775: GenCollectForAllocation          [       8          0              0    ]      "
                + "[     0     0     0     0     0    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SAFEPOINT.toString() + ".",
                SafepointEvent.match(logLine));
        SafepointEvent event = new SafepointEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 47775, event.getTimestamp());
        Assert.assertEquals("Trigger not parsed correctly.", JdkUtil.TriggerType.GEN_COLLECT_FOR_ALLOCATION,
                event.getTriggerType());
        Assert.assertEquals("Total number of threads stopped in safepoint not parsed correctly.", 8,
                event.getThreadsTotal());
        Assert.assertEquals("Number of threads that were spinning before safepoint not parsed correctly.", 0,
                event.getThreadsSpinning());
        Assert.assertEquals("Number of threads that were blocked before safepoint not parsed correctly.", 0,
                event.getThreadsBlocked());
        Assert.assertEquals("Time for spinning threads to reach safepoint.", 0, event.getTimeSpin());
        Assert.assertEquals("Time for blocked threads to reach safepoint not parsed correctly.", 0,
                event.getTimeBlock());
        Assert.assertEquals("Time for all threads to reach safepoint (sync) not parsed correctly.", 0,
                event.getTimeSync());
        Assert.assertEquals("Time for cleanup activities not parsed correctly.", 0, event.getTimeCleanup());
        Assert.assertEquals("Time for safepoint activity (vmop) not parsed correctly.", 0, event.getTimeVmop());
        Assert.assertEquals("Page trap count not parsed correctly.", 0, event.getPageTrapCount());
        Assert.assertEquals("Duration not calculated correctly.", 0, event.getDuration());
    }
}
