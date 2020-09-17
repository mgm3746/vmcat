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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeaderEvent extends TestCase {

    public void testParseLogLine() {
        String logLine = "         vmop                    [threads: total initially_running wait_to_block]    "
                + "[time: spin block sync cleanup vmop] page_trap_count";
        Assert.assertTrue(JdkUtil.LogEventType.HEADER.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeaderEvent);
    }

    public void testReportable() {
        String logLine = "         vmop                    [threads: total initially_running wait_to_block]    "
                + "[time: spin block sync cleanup vmop] page_trap_count";
        Assert.assertFalse(JdkUtil.LogEventType.HEADER.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "         vmop                    [threads: total initially_running wait_to_block]    "
                + "[time: spin block sync cleanup vmop] page_trap_count";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER.toString() + ".",
                HeaderEvent.match(logLine));
    }

    public void testLogLineJdk11() {
        String logLine = "          vmop                            [ threads:    total initially_running "
                + "wait_to_block ][ time:    spin   block    sync cleanup    vmop ] page_trap_count";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER.toString() + ".",
                HeaderEvent.match(logLine));
    }
}
