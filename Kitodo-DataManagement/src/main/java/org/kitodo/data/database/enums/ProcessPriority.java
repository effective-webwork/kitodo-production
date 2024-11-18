/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.database.enums;

import java.util.Objects;

/**
 * Enum for process priority:
 *  - 0 = low priority
 *  - 1 = normal priority
 *  - 2 = high priority
 */
public enum ProcessPriority {

    LOW(0, "low"),
    NORMAL(1, "normal"),
    HIGH(2, "high");

    private final int value;
    private final String priorityString;

    ProcessPriority(int intPriority, String stringPriority) {
        this.value = intPriority;
        this.priorityString = stringPriority;
    }

    public int getValue() {
        return this.value;
    }

    public String getPriorityString() {
        return this.priorityString;
    }

    public static ProcessPriority getPriorityFromValue(Integer value) {
        if (Objects.nonNull(value)) {
            for (ProcessPriority priority : values()) {
                if (priority.getValue() == value) {
                    return priority;
                }
            }
        }
        return NORMAL;
    }
}
