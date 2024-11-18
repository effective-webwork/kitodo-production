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

package org.kitodo.config.enums;

public enum ParameterDataEditor implements ParameterInterface {

    METS_EDITOR_DEFAULT_PAGINATION("metsEditor.defaultPagination");

    private String name;

    /**
     * Private constructor to hide the implicit public one.
     *
     * @param name
     *            of parameter
     */
    ParameterDataEditor(String name) {
        this.name = name;
    }

    @Override
    public java.lang.String toString() {
        return this.name;
    }
}
