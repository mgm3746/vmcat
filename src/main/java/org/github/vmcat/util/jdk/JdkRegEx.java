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

/**
 * Regular expression utility methods and constants for OpenJDK and Sun JDK.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JdkRegEx {

    /**
     * Blank line.
     */
    public static final String BLANK_LINE = "^\\s+$";

    /**
     * Datestamp. Absolute date/time the JVM uses with <code>-XX:+PrintGCDateStamps</code>.
     * 
     * For example:
     * 
     * 1) Minus GMT: 2010-02-26T09:32:12.486-0600
     * 
     * 2) Plus GMT: 2010-04-16T12:11:18.979+0200
     */
    public static final String DATESTAMP = "((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(-|\\+)"
            + "(\\d{4}))";

    /**
     * Regular expression for recognized decorations prepending logging.
     * 
     * <p>
     * 1) uptime: 1668.902:
     * </p>
     */
    public static final String DECORATOR = "(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ":";

    /**
     * The number of threads or page trap count.
     * 
     * For example: 123
     */
    public static final String NUMBER = "(\\d{1,8})";

    /**
     * Regular expression for thread data.
     * 
     * For example:
     * 
     * <pre>
     * [    2404          0              1    ]
     * </pre>
     */
    public static final String THREAD_BLOCK = "\\[[ ]{4,6}" + JdkRegEx.NUMBER + "[ ]{9,10}" + JdkRegEx.NUMBER
            + "[ ]{12,14}" + JdkRegEx.NUMBER + "[ ]{4}\\]";

    /**
     * The time as a whole number representing milliseconds.
     * 
     * For example: 7291
     */
    public static final String TIME = "(\\d{1,8})";

    /**
     * Regular expression for times data.
     * 
     * For example:
     * 
     * <pre>
     * [    13    55    73    29  1387    ]
     * </pre>
     */
    public static final String TIMES_BLOCK = "\\[[ ]{1,5}" + JdkRegEx.TIME + "[ ]{1,5}" + JdkRegEx.TIME + "[ ]{1,5}"
            + JdkRegEx.TIME + "[ ]{1,5}" + JdkRegEx.TIME + "[ ]{1,5}" + JdkRegEx.TIME + "[ ]{1,5}\\]";

    /**
     * Timestamp. Milliseconds since JVM started.
     * 
     * For example: 487.020
     */
    public static final String TIMESTAMP = "(\\d{0,12}[\\.\\,]\\d{3})";

    /**
     * <p>
     * Bulk operation when the compiler has to recompile previously compiled code due to the compiled code no longer
     * being valid (e.g. a dynamic object has changed) or with tiered compilation when client compiled code is replaced
     * with server compiled code.
     * </p>
     */
    public static final String TRIGGER_BULK_REVOKE_BIAS = "BulkRevokeBias";

    /**
     * <p>
     * When the Metaspace is resized. The JVM has failed to allocate memory for something that should be stored in
     * Metaspace and does a full collection before attempting to resize the Metaspace.
     * </p>
     */
    public static final String TRIGGER_COLLECT_FOR_METADATA_ALLOCATION = "CollectForMetadataAllocation";

    /**
     * <p>
     * When the compiler has to recompile previously compiled code due to the compiled code no longer being valid (e.g.
     * a dynamic object has changed) or with tiered compilation when client compiled code is replaced with server
     * compiled code.
     * </p>
     */
    public static final String TRIGGER_DEOPTIMIZE = "Deoptimize";

    /**
     * <p>
     * Biased locking is an optimization to reduce the overhead of uncontested locking. It assumes a thread owns a
     * monitor until another thread tries to acquire it.
     * </p>
     * 
     * <p>
     * EnableBiasedLocking is the operation the JVM does on startup when BiasedLocking is enabled (default for JDK8 and
     * 11).
     * </p>
     * 
     * <p>
     * BiasedLocking is being disabled and deprecated in JDK 17, as it's typically not relevant to modern workloads:
     * https://bugs.openjdk.java.net/browse/JDK-8231265.
     * </p>
     */
    public static final String TRIGGER_ENABLE_BIASED_LOCKING = "EnableBiasedLocking";

    /**
     * <p>
     * TODO:
     * </p>
     */
    public static final String TRIGGER_FIND_DEADLOCKS = "FindDeadlocks";

    /**
     * <p>
     * TODO:
     * </p>
     */
    public static final String TRIGGER_FORCE_SAFEPOINT = "ForceSafepoint";

    /**
     * <p>
     * Guaranteed safepoint to process non-urgent JVM operations. The interval is enabled by
     * <code>-XX:+UnlockDiagnosticVMOptions</code> and controlled by <code>-XX:GuaranteedSafepointInterval=N</code>
     * (default 300000 seconds = 5 minutes).
     * </p>
     */
    public static final String TRIGGER_NO_VM_OPERATION = "no vm operation";

    /**
     * <p>
     * Concurrent Mark Sweep (CMS) collection.
     * </p>
     */
    public static final String TRIGGER_PARALLEL_GC_FAILED_ALLOCATION = "ParallelGCFailedAllocation";

    /**
     * <p>
     * Concurrent Mark Sweep (CMS) collection initiated by explicit gc.
     * </p>
     */
    public static final String TRIGGER_PARALLEL_GC_SYSTEM_GC = "ParallelGCSystemGC";

    /**
     * <p>
     * TODO:
     * </p>
     */
    public static final String TRIGGER_PRINT_JNI = "PrintJNI";

    /**
     * <p>
     * Printing a stack trace.
     * </p>
     */
    public static final String TRIGGER_PRINT_THREADS = "PrintThreads";

    /**
     * <p>
     * Biased locking is an optimization to reduce the overhead of uncontested locking. It assumes a thread owns a
     * monitor until another thread tries to acquire it.
     * </p>
     * 
     * <p>
     * RevokeBias is the operation the JVM does to undo the optimization when a different thread tries to acquire the
     * monitor.
     * </p>
     * 
     * <p>
     * BiasedLocking is being disabled and deprecated in JDK 17, as it's typically not relevant to modern workloads:
     * https://bugs.openjdk.java.net/browse/JDK-8231265.
     * </p>
     */
    public static final String TRIGGER_REVOKE_BIAS = "RevokeBias";

    /**
     * <p>
     * Generating a thread dump.
     * </p>
     */
    public static final String TRIGGER_THREAD_DUMP = "ThreadDump";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkRegEx() {

    }

}
