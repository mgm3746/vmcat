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
 * Regular expression utility methods and constants for OpenJDK and Oracle JDK.
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
    public static final String THREAD_BLOCK = "\\[[ ]{4,7}" + JdkRegEx.NUMBER + "[ ]{9,10}" + JdkRegEx.NUMBER
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
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkRegEx() {

    }

}
