--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.
--

SET SQL_SAFE_UPDATES = 0;

-- add authorities/permission change the order of media in the metadata editor
INSERT IGNORE INTO authority (title) VALUES ('moveMedia_clientAssignable');

SET SQL_SAFE_UPDATES = 1;
