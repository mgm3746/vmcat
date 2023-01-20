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
package org.github.vmcat.domain.jdk;

import org.github.vmcat.util.jdk.Safepoint.Trigger;

/**
 * <code>SafepointEvent</code> summary used for reporting
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class SafepointEventSummary {

    /**
     * The <code>Trigger</code>
     */
    private Trigger trigger;

    /**
     * Total number of events.
     */
    private long count;

    /**
     * Total pause time (milliseconds).
     */
    private long pauseTotal;

    /**
     * Max pause time (milliseconds).
     */
    private int pauseMax;

    /**
     * Default constructor.
     * 
     * @param trigger
     *            The <code>Trigger</code>.
     * @param count
     *            Number of events.
     * @param pauseTotal
     *            Total pause time of events
     * @param pauseMax
     *            Max pause time of events
     */
    public SafepointEventSummary(Trigger trigger, long count, long pauseTotal, int pauseMax) {
        this.trigger = trigger;
        this.count = count;
        this.pauseTotal = pauseTotal;
        this.pauseMax = pauseMax;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public long getCount() {
        return count;
    }

    public long getPauseTotal() {
        return pauseTotal;
    }

    public long getPauseMax() {
        return pauseMax;
    }
}
