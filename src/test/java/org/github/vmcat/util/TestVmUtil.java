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
package org.github.vmcat.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestVmUtil extends TestCase {

    public void testPartialLog() {
        Assert.assertFalse("Not a partial log.", VmUtil.isPartialLog(59999));
        Assert.assertTrue("Is a partial log.", VmUtil.isPartialLog(60001));
    }

    public void testHtmlStartTag() {
        String tag = "<name>";
        Assert.assertTrue("'" + tag + "' not identified as a start tag.", VmUtil.isHtmlEventStartTag(tag));
    }

    public void testHtmlEndTag() {
        String tag = "</name>";
        Assert.assertFalse("'" + tag + "' incorrectly identified as a start tag.", VmUtil.isHtmlEventStartTag(tag));
    }
}
