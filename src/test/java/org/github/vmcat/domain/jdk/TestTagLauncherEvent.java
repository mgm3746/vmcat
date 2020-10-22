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

import java.io.File;

import org.github.vmcat.domain.Jvm;
import org.github.vmcat.domain.JvmRun;
import org.github.vmcat.service.Manager;
import org.github.vmcat.util.Constants;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTagLauncherEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "<launcher>";
        Assert.assertTrue(JdkUtil.LogEventType.TAG_LAUNCHER.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof TagLauncherEvent);
    }

    public void testReportable() {
        String logLine = "<launcher>";
        Assert.assertFalse(JdkUtil.LogEventType.TAG_LAUNCHER.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "<launcher>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_LAUNCHER.toString() + ".",
                TagLauncherEvent.match(logLine));
    }

    public void testLogLineEndTag() {
        String logLine = "</launcher>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_LAUNCHER.toString() + ".",
                TagLauncherEvent.match(logLine));
    }

    public void testLauncherBlock() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset7.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm(), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " vent identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.TAG_LAUNCHER.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.TAG_LAUNCHER));
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
    }
}
