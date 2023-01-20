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
package org.github.vmcat.util.jdk;

import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkRegEx extends TestCase {

    public void testTimestampWithCharacter() {
        String timestamp = "A.123";
        Assert.assertFalse("Timestamps are decimal numbers.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithFewerDecimalPlaces() {
        String timestamp = "1.12";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithMoreDecimalPlaces() {
        String timestamp = "1.1234";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithNoDecimal() {
        String timestamp = "11234";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampLessThanOne() {
        String timestamp = ".123";
        Assert.assertTrue("Timestamps less than one do not have a leading zero.",
                timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampValid() {
        String timestamp = "1.123";
        Assert.assertTrue("'" + timestamp + "' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampDecimalComma() {
        String timestamp = "1,123";
        Assert.assertTrue("'" + timestamp + "' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testDatestampGmtMinus() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        Assert.assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    public void testDatestampGmtPlus() {
        String datestamp = "2010-04-16T12:11:18.979+0200";
        Assert.assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    public void testDecoratorTimeUptime() {
        String decorator = "2020-03-10T08:03:29.311-0400: 0.373:";
        Assert.assertTrue("'" + decorator + "' " + "not a valid decorator.", decorator.matches(JdkRegEx.DECORATOR));
    }

    public void testDecoratorUptime() {
        String decorator = "1530.682:";
        Assert.assertTrue("'" + decorator + "' " + "not a valid decorator.", decorator.matches(JdkRegEx.DECORATOR));
    }

    public void testTime() {
        String time = "12345678";
        Assert.assertTrue("'" + time + "' " + "not a valid time.", time.matches(JdkRegEx.TIME));
    }

    public void testThread() {
        String threads = "12345678";
        Assert.assertTrue("'" + threads + "' " + "not a valid number of threads.", threads.matches(JdkRegEx.NUMBER));
    }
}
