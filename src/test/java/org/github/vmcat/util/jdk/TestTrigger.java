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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTrigger extends TestCase {

    public void testTriggerIdentity() {
        Trigger.TriggerType[] triggers = Trigger.TriggerType.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(Trigger.TriggerType.UNKNOWN)) {
                Assert.assertFalse(triggers[i].name() + " not idenitified.",
                        Trigger.identifyTriggerType(triggers[i].name()).equals(Trigger.TriggerType.UNKNOWN));
            }
        }
    }

    public void testTriggerLiteral() {
        Trigger.TriggerType[] triggers = Trigger.TriggerType.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(Trigger.TriggerType.UNKNOWN)) {
                try {
                    Trigger.getTriggerLiteral(triggers[i]);
                } catch (AssertionError e) {
                    Assert.fail(triggers[i].name() + " literal not found.");
                }
            }
        }
    }

}
