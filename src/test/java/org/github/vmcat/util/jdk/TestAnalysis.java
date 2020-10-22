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
package org.github.vmcat.util.jdk;

import java.io.File;

import org.github.vmcat.domain.Jvm;
import org.github.vmcat.domain.JvmRun;
import org.github.vmcat.service.Manager;
import org.github.vmcat.util.Constants;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestAnalysis extends TestCase {

    /**
     * Verify analysis file property key/value lookup.
     */
    public void testPropertyKeyValueLookup() {
        Analysis[] analysis = Analysis.values();
        for (int i = 0; i < analysis.length; i++) {
            Assert.assertNotNull(analysis[i].getKey() + " not found.", analysis[i].getValue());
        }
    }

    public void testPartialLog() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset1.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm(), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED));

    }

    public void testUnknownLogLine() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset10.txt");
        Manager manager = new Manager();
        manager.store(testFile);
        JvmRun jvmRun = manager.getJvmRun(new Jvm(), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT));

    }

}