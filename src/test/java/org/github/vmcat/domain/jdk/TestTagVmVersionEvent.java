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
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTagVmVersionEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "<vm_version>";
        Assert.assertTrue(JdkUtil.LogEventType.TAG_VM_VERSION.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof TagVmVersionEvent);
    }

    public void testReportable() {
        String logLine = "<vm_version>";
        Assert.assertFalse(JdkUtil.LogEventType.TAG_VM_VERSION.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "<vm_version>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_VM_VERSION.toString() + ".",
                TagVmVersionEvent.match(logLine));
    }

    public void testLogLineEndTag() {
        String logLine = "</vm_version>";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TAG_VM_VERSION.toString() + ".",
                TagVmVersionEvent.match(logLine));
    }
}
