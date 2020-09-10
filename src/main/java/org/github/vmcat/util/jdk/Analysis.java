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

import org.github.vmcat.util.Constants;
import org.github.vmcat.util.GcUtil;

/**
 * Analysis constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public enum Analysis {

    /**
     * Property key for TBD.
     */
    ERROR_TBD("error.tbd"),


    /**
     * Property key for TBD.
     */
    INFO_TBD("info.tbd"),


    /**
     * Property key for TBD.
     */
    WARN_TBD("warn.tbd");

    private String key;

    private Analysis(final String key) {
        this.key = key;
    }

    /**
     * @return Analysis property file key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return Analysis property file value.
     */
    public String getValue() {
        return GcUtil.getPropertyValue(Constants.ANALYSIS_PROPERTY_FILE, key);
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
