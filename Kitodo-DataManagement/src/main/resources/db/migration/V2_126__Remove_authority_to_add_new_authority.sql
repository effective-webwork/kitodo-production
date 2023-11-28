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

--
-- Migration: Remove authorities to add or edit authorities.
-- 1. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Delete authorities to add, view or edit authorities from cross table
--
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title='addAuthority_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title='editAuthority_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title='viewAuthority_globalAssignable');

-- 3. Delete authorities to add, view or edit authorities from authorities table
--
DELETE FROM authority WHERE title='addAuthority_globalAssignable';
DELETE FROM authority WHERE title='editAuthority_globalAssignable';
DELETE FROM authority WHERE title='viewAuthority_globalAssignable';

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;