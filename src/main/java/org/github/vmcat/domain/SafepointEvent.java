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

/**
 * <p>
 * Base safepoint event (all threads stopped).
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface SafepointEvent extends LogEvent {

    /**
     * @return The total number of threads stopped in safepoint.
     */
    int getThreadsTotal();

    /**
     * @return The number of threads that were spinning before safepoint.
     */
    int getThreadsSpinning();

    /**
     * @return The number of threads that were blocked before safepoint.
     */
    int getThreadsBlocked();

    /**
     * @return The time it took spinning threads to reach safepoint in milliseconds.
     */
    int getTimeSpin();

    /**
     * @return The time it took blocked threads to reach safepoint in milliseconds.
     */
    int getTimeBlock();

    /**
     * @return The time it took all threads to reach safepoint in milliseconds.
     */
    int getTimeSync();

    /**
     * @return The time it took for cleanup activities in milliseconds.
     */
    int getTimeCleanup();

    /**
     * @return The time it took for the safepoint activity in milliseconds.
     */
    int getTimeVmop();

    /**
     * @return The page trap count.
     */
    int getPageTrapCount();

}
