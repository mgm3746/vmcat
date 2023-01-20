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
package org.github.vmcat.domain.jdk;

import org.github.vmcat.util.jdk.JdkUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestFooterStatsEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "Polling page always armed";
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof FooterStatsEvent);
    }

    public void testReportable() {
        String logLine = "Polling page always armed";
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "Polling page always armed";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineDeoptimize() {
        String logLine = "Deoptimize                         12";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineGenCollectForAllocation() {
        String logLine = "GenCollectForAllocation        1027";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineEnableBiasedLocking() {
        String logLine = "EnableBiasedLocking                1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineVmOperationsCoalesced() {
        String logLine = "    0 VM operations coalesced during safepoint";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineMaximumSyncTime() {
        String logLine = "Maximum sync time      0 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineMaximumVmOperationTime() {
        String logLine = "Maximum vm operation time (except for Exit VM operation)     23 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLogLineExit() {
        String logLine = "Exit                               1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }
}
