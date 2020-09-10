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
package org.github.vmcat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.github.vmcat.Main;
import org.github.vmcat.domain.JvmRun;
import org.github.vmcat.domain.LogEvent;
import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.domain.UnknownEvent;
import org.github.vmcat.hsql.JvmDao;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.Jvm;

/**
 * <p>
 * Provides vm log analysis services to other layers.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Manager {

    /**
     * The JVM data access object.
     */
    private JvmDao jvmDao;

    /**
     * Default constructor.
     */
    public Manager() {
        this.jvmDao = new JvmDao();
    }

    /**
     * Parse the vm logging for the JVM run and store the data in the data store.
     * 
     * @param logFile
     *            The vm log file.
     */
    public void store(File logFile) {

        if (logFile == null) {
            return;
        }

        // Parse vm log file
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(logFile));
            String logLine = bufferedReader.readLine();
            while (logLine != null) {
                LogEvent event = JdkUtil.parseLogLine(logLine);
                if (event instanceof SafepointEvent) {
                    jvmDao.addSafepointEvent((SafepointEvent) event);
                } else if (event instanceof UnknownEvent) {
                    if (jvmDao.getUnidentifiedLogLines().size() < Main.REJECT_LIMIT) {
                        jvmDao.getUnidentifiedLogLines().add(logLine);
                    }
                }
                logLine = bufferedReader.readLine();
            }

            // Process final batch
            jvmDao.processSafepointBatch();

        } catch (

        FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Get JVM run data.
     * 
     * @param jvm
     *            JVM environment information.
     * @return The JVM run data.
     */
    public JvmRun getJvmRun(Jvm jvm) {
        JvmRun jvmRun = new JvmRun(jvm);
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.setAnalysis(jvmDao.getAnalysis());
        jvmRun.doAnalysis();
        return jvmRun;
    }
}
