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

package org.kitodo.data.database.persistence;

import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Report;
import org.kitodo.data.database.exceptions.DAOException;

public class ReportDAO extends BaseDAO<Report> {

    /**
     * Retrieves a BaseBean identified by the given id from the database.
     *
     * @param reportId of bean to load
     * @return persisted bean
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public Report getById(Integer reportId) throws DAOException {
        Report report = retrieveObject(Report.class, reportId);
        if (Objects.isNull(report)) {
            throw new DAOException("Unable to find report with ID " + reportId + " in database.");
        }
        return report;
    }

    public Report getByKitodoId(Integer kitodoId) {
        List<Report> reports = getByQuery("FROM Report WHERE kitodoId = " + kitodoId);
        if (reports.isEmpty()) {
            return null;
        } else {
            return reports.get(0);
        }
    }

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<Report> getAll() throws DAOException {
        return retrieveAllObjects(Report.class);
    }

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<Report> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Report ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieves all not indexed BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<Report> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes BaseBean object specified by the given id from the database.
     *
     * @param reportId of bean to delete
     * @throws DAOException if the current session can't be retrieved or an exception is
     *                      thrown while performing the rollback
     */
    @Override
    public void remove(Integer reportId) throws DAOException {
        removeObject(Report.class, reportId);
    }
}
