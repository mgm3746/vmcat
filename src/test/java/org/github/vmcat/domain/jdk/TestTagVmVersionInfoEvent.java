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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTagVmVersionInfoEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "<info>";
        Assert.assertTrue(JdkUtil.LogEventType.TAG_VM_VERSION_INFO.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof TagVmVersionInfoEvent);
    }

    public void testReportable() {
        String logLine = "<info>";
        Assert.assertFalse(
                JdkUtil.LogEventType.TAG_VM_VERSION_INFO.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "<info>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_VM_VERSION_INFO.toString() + ".",
                TagVmVersionInfoEvent.match(logLine));
    }

    public void testLogLineEndTag() {
        String logLine = "</info>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_VM_VERSION_INFO.toString() + ".",
                TagVmVersionInfoEvent.match(logLine));
    }

    public void testVersionInfoBlock() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset4.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " vent identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.TAG_VM_VERSION_INFO.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.TAG_VM_VERSION_INFO));
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        String version = "OpenJDK 64-Bit Server VM (25.242-b08-debug) for linux-amd64 JRE (1.8.0_242-b08), built on "
                + "Jan 15 2020 17:24:19 by &quot;mockbuild&quot; with gcc 4.8.5 20150623 (Red Hat 4.8.5-39)";
        Assert.assertEquals("JDK version not correct.", version, jvmRun.getJvm().getVersion());
    }
}
