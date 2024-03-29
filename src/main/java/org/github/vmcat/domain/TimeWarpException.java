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
package org.github.vmcat.domain;

/**
 * Exception when the {@link org.github.vmcat.domain.jdk.SafepointEvent} chronology is not possible. For example, a
 * {@link org.github.vmcat.domain.jdk.SafepointEvent} that starts before a previous
 * {@link org.github.vmcat.domain.jdk.SafepointEvent} finishes.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
@SuppressWarnings("serial")
public class TimeWarpException extends RuntimeException {

    public TimeWarpException(String string) {
        super(string);
    }

}
