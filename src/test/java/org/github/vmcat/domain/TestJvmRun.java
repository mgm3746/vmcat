/**********************************************************************************************************************
 * vmcat                                                                                                              *
 *                                                                                                                    *
 * Copyright (c) 2020-2023 Mike Millson                                                                                    *
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

import java.io.File;

import org.github.vmcat.service.Manager;
import org.github.vmcat.util.Constants;
import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmRun extends TestCase {

    public void testSummaryStats() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset9.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm(), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER.toString() + " event not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER));
        Assert.assertTrue(JdkUtil.LogEventType.SAFEPOINT.toString() + " event not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SAFEPOINT));
        Assert.assertEquals("Safepoint event count not correct.", 9, jvmRun.getSafepointEventCount());
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertEquals("Safepoint first timestamp not correct.", 7723,
                jvmRun.getFirstSafepointEvent().getTimestamp());
        Assert.assertEquals("Safepoint last timestamp not correct.", 10756,
                jvmRun.getLastSafepointEvent().getTimestamp());
        Assert.assertEquals("Safepoint total pause not correct.", 440, jvmRun.getSafepointTotalPause());
        Assert.assertEquals("Safepoint last duration not correct.", 33, jvmRun.getLastSafepointEvent().getDuration());
        Assert.assertEquals("JVM run duration not correct.", 10789, jvmRun.getJvmRunDuration());
        Assert.assertEquals("Throughput not correct.", 96, jvmRun.getThroughput());
        Assert.assertFalse(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT));
    }

    public void testSummaryStatsPartialLog() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset1.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm(), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER.toString() + " event not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER));
        Assert.assertTrue(JdkUtil.LogEventType.SAFEPOINT.toString() + " event not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SAFEPOINT));
        Assert.assertEquals("Safepoint event count not correct.", 9, jvmRun.getSafepointEventCount());
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertEquals("Safepoint first timestamp not correct.", 1617723,
                jvmRun.getFirstSafepointEvent().getTimestamp());
        Assert.assertEquals("Safepoint last timestamp not correct.", 1620756,
                jvmRun.getLastSafepointEvent().getTimestamp());
        Assert.assertEquals("Safepoint total pause not correct.", 440, jvmRun.getSafepointTotalPause());
        Assert.assertEquals("Safepoint last duration not correct.", 33, jvmRun.getLastSafepointEvent().getDuration());
        Assert.assertEquals("JVM run duration not correct.", 3066, jvmRun.getJvmRunDuration());
        Assert.assertEquals("Throughput not correct.", 86, jvmRun.getThroughput());
        Assert.assertFalse(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT));
    }
}
