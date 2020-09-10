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
public class TestRevokeBiasEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        Assert.assertTrue(JdkUtil.LogEventType.REVOKE_BIAS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof RevokeBiasEvent);
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.REVOKE_BIAS;
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        Assert.assertTrue(JdkUtil.LogEventType.REVOKE_BIAS.toString() + " not parsed.",
                JdkUtil.hydrateSafepointEvent(eventType, logLine) instanceof RevokeBiasEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.REVOKE_BIAS.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.REVOKE_BIAS));
    }

    public void testLogLine() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REVOKE_BIAS.toString() + ".",
                RevokeBiasEvent.match(logLine));
        RevokeBiasEvent event = new RevokeBiasEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1665730, event.getTimestamp());
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
    }

    public void testLogLineWhiteSpaceAtEnd() {
        String logLine = "1665.730: RevokeBias                       [    2409          2             74    ]      "
                + "[     3     2    10    30     0    ]  1     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REVOKE_BIAS.toString() + ".",
                RevokeBiasEvent.match(logLine));
    }

    public void testLogLineAlternateSpaces() {
        String logLine = "9.406: RevokeBias                       [      22          0              1    ]      "
                + "[     0     0     0     0     0    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REVOKE_BIAS.toString() + ".",
                RevokeBiasEvent.match(logLine));
    }

    public void testLogLineAlternateSpaces2() {
        String logLine = "13.738: RevokeBias                       [      25          0              3    ]      "
                + "[     0    12    12     0     7    ]  0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REVOKE_BIAS.toString() + ".",
                RevokeBiasEvent.match(logLine));
    }
}
