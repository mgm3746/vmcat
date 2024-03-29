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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Math utility methods and constants for OpenJDK and Oracle JDK.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JdkMath {

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkMath() {

    }

    /**
     * Convert seconds to milliseconds. For example: Convert 0.0225213 to 23.
     * 
     * @param secs
     *            Seconds as a whole number or decimal.
     * @return Milliseconds rounded to a whole number.
     */
    public static BigDecimal convertSecsToMillis(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal millis = new BigDecimal(secs.replace(",", "."));
        millis = millis.movePointRight(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        millis = millis.setScale(0, RoundingMode.DOWN);
        return millis;
    }

    /**
     * Round milliseconds to whole number.
     * 
     * For example: Convert 2.969 to 2
     * 
     * @param millis
     *            Milliseconds with decimal places.
     * @return Milliseconds rounded to a whole number.
     */
    public static BigDecimal roundMillis(String millis) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal rounded = new BigDecimal(millis.replace(",", "."));
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        rounded = rounded.setScale(0, RoundingMode.DOWN);
        return rounded;
    }

    /**
     * Convert milliseconds to seconds.
     * 
     * For example: Convert 123456 123.456.
     * 
     * @param millis
     *            Milliseconds as a whole number.
     * @return Seconds rounded to 3 decimal places.
     */
    public static BigDecimal convertMillisToSecs(long millis) {
        BigDecimal secs = new BigDecimal(millis);
        secs = secs.movePointLeft(3);
        secs = secs.setScale(3, RoundingMode.HALF_EVEN);
        return secs;
    }

    /**
     * Convert seconds to microseconds.
     * 
     * For example: Convert 0.0225213 to 23521
     * 
     * @param secs
     *            Seconds as a whole number or decimal.
     * @return Microseconds rounded to a whole number.
     */
    public static BigDecimal convertSecsToMicros(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal micros = new BigDecimal(secs.replace(",", "."));
        micros = micros.movePointRight(6);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        micros = micros.setScale(0, RoundingMode.DOWN);
        return micros;
    }

    /**
     * Convert milliseconds to microseconds.
     * 
     * For example: Convert 0.003 to 3
     * 
     * @param millis
     *            Milliseconds as a whole number or decimal.
     * @return Microseconds rounded to a whole number.
     */
    public static BigDecimal convertMillisToMicros(String millis) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal duration = new BigDecimal(millis.replace(",", "."));
        duration = duration.movePointRight(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        duration = duration.setScale(0, RoundingMode.DOWN);
        return duration;
    }

    /**
     * Convert microseconds to milliseconds.
     * 
     * For example: Convert 987654321 987.654.
     * 
     * @param micros
     *            Microseconds as a whole number.
     * @return Milliseconds rounded to 3 decimal places.
     */
    public static BigDecimal convertMicrosToMillis(long micros) {
        BigDecimal duration = new BigDecimal(micros);
        duration = duration.movePointLeft(3);
        duration = duration.setScale(3, RoundingMode.HALF_EVEN);
        return duration;
    }

    /**
     * Convert seconds to centiseconds. For example: Convert 1.02 to 102.
     * 
     * @param secs
     *            Seconds as a number with 2 decimal places.
     * @return Centoseconds.
     */
    public static BigDecimal convertSecsToCentis(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal duration = new BigDecimal(secs.replace(",", "."));
        duration = duration.movePointRight(2);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        duration = duration.setScale(0, RoundingMode.DOWN);
        return duration;
    }

    /**
     * Add together an array of durations and convert seconds to milliseconds.
     * 
     * Useful for the CMS Remark phase, where a single event can have multiple phases and durations.
     * 
     * For example: 0.0226730 + 0.0624566 + 0.0857010 = .1708306 seconds = 171 milliseconds
     * 
     * @param durations
     *            <code>Array</code> of seconds as whole numbers and/or decimals.
     * @return Total time rounded to a whole number.
     */
    public static int totalDuration(String[] durations) {
        BigDecimal duration = new BigDecimal("0");
        for (int i = 0; i < durations.length; i++) {
            // BigDecimal does not accept decimal commas, only decimal periods
            duration = duration.add(new BigDecimal(durations[i].replace(",", ".")));
        }
        return convertSecsToMillis(duration.toPlainString()).intValue();
    }

    /**
     * Calculate the throughput between two safepoint events. Throughput is the percent of time not spent in safepoint.
     * 
     * @param currentDuration
     *            Current event time spent in safepoint (milliseconds) beginning at currentTimestamp.
     * @param currentTimestamp
     *            Current safepoint event timestamp (milliseconds after JVM startup).
     * @param priorDuration
     *            Prior safepoint event time in safepoint (milliseconds) beginning at priorTimestamp. 0 for the first
     *            event.
     * @param priorTimestamp
     *            Prior safepoint event timestamp (milliseconds after JVM startup). 0 for the first event.
     * @return Throughput as a percent. 0 means all time was spent in safepoint. 100 means no time was spent in
     *         safepoint.
     */
    public static int calcThroughput(final int currentDuration, final long currentTimestamp, final int priorDuration,
            final long priorTimestamp) {
        long timeTotal = currentTimestamp + new Long(currentDuration).longValue() - priorTimestamp;
        long timeNotGc = timeTotal - new Long(currentDuration).longValue() - new Long(priorDuration).longValue();
        BigDecimal throughput = new BigDecimal(timeNotGc);
        throughput = throughput.divide(new BigDecimal(timeTotal), 2, RoundingMode.HALF_EVEN);
        throughput = throughput.movePointRight(2);
        return throughput.intValue();
    }

    /**
     * Calculate size in kilobytes.
     * 
     * @param size
     *            Size block value.
     * @param units
     *            Size block units.
     * @return The size in Kilobytes.
     */
    public static int calcKilobytes(final int size, final char units) {
        int kilobytes = size;
        switch (units) {
        case 'M':
            kilobytes = kilobytes * 1024;
            break;
        case 'G':
            kilobytes = kilobytes * 1024 * 1024;
            break;
        }
        return kilobytes;
    }

    /**
     * Convert SIZE to kilobytes.
     * 
     * @param size
     *            The size (e.g. '128.0', 306,0).
     * @param units
     *            The units (e.g. 'G').
     * @return The size in Kilobytes.
     */
    public static int convertSizeToKilobytes(final String size, final char units) {

        BigDecimal kilobytes = new BigDecimal(size.replace(",", "."));
        BigDecimal kilo = new BigDecimal("1024");

        switch (units) {

        case 'B':
            kilobytes = kilobytes.divide(new BigDecimal("1024"));
            break;
        case 'K':
            break;
        case 'M':
            kilobytes = kilobytes.multiply(kilo);
            break;
        case 'G':
            kilobytes = (kilobytes.multiply(kilo)).multiply(kilo);
            break;
        default:
            throw new AssertionError("Unexpected units value: " + units);

        }
        kilobytes = kilobytes.setScale(0, RoundingMode.HALF_EVEN);
        return kilobytes.intValue();
    }

    /**
     * @param parallelism
     *            The parallelism percent (ratio or user to wall (real time).
     * 
     * @return True if the parallelism is "inverted", false otherwise. Inverted parallelism is &lt;= 100. In other
     *         words, the parallel collection performance is less than serial (single-threaded).
     */
    public static boolean isInvertedParallelism(int parallelism) {
        return (parallelism < 100);
    }

    /**
     * @param parallelism
     *            The parallelism percent (ratio or user to wall (real time).
     * 
     * @return True if the parallelism is low (&lt; 2 threads, rounded), false otherwise.
     */
    public static boolean isLowParallelism(int parallelism) {
        return (parallelism < 150);
    }
}
