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
package org.github.vmcat.domain;

import java.util.List;

import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.Jvm;

/**
 * JVM run data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmRun {

    /**
     * JVM environment information.
     */
    private Jvm jvm;

    /**
     * Total number of safepoint events.
     */
    private int safepointEventCount;

    /**
     * Log lines that do not match any existing logging patterns.
     */
    private List<String> unidentifiedLogLines;

    /**
     * Analysis.
     */
    private List<Analysis> analysis;

    /**
     * Constructor accepting throughput threshold, JVM services, and JVM environment information.
     * 
     * @param jvm
     *            JVM environment information.
     */
    public JvmRun(Jvm jvm) {
        this.jvm = jvm;
    }

    public Jvm getJvm() {
        return jvm;
    }

    public void setJvm(Jvm jvm) {
        this.jvm = jvm;
    }

    public int getSafepointEventCount() {
        return safepointEventCount;
    }

    public void setSafepointEventCount(int safepointEventCount) {
        this.safepointEventCount = safepointEventCount;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public void setUnidentifiedLogLines(List<String> unidentifiedLogLines) {
        this.unidentifiedLogLines = unidentifiedLogLines;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    /**
     * Do analysis.
     */
    public void doAnalysis() {

        if (jvm.getOptions() != null) {
            doJvmOptionsAnalysis();
        }
        // TODO:
        if (haveData()) {
            doDataAnalysis();
        }
    }

    /**
     * Do data analysis.
     */
    private void doDataAnalysis() {
        // TODO:
    }

    /**
     * @return true if there is data, false otherwise (e.g. no logging lines recognized).
     */
    public boolean haveData() {
        boolean haveData = true;
        // TODO:
        return haveData;
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // TODO:
    }

}
