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

-- Change user group and role.

-- 1. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Delete admin authority from cross table
--
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'admin_globalAssignable');

-- 3. Drop admin authority
--
DELETE FROM authority WHERE title = 'admin_globalAssignable';

-- 4. Delete global authorities which shouldn't be global from cross table
--
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProjects_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Project_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllTemplates_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProcesses_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Process_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllBatches_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Batch_globalAssignable');

-- 5. Drop global authorities which should not be global
--
DELETE FROM authority WHERE title = 'viewAllProjects_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Project_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllTemplates_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Template_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllWorkflows_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Workflow_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllDockets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Docket_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllRulesets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Ruleset_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllProcesses_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Process_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllBatches_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Batch_globalAssignable';

-- 6. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 7. Add initial roles if they do not exist yet
--
INSERT IGNORE INTO role (title, client_id) VALUES ('AdminGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('CheckDossierGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('PrepareDossierGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ScanDossierAGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ScanDossierBGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('CheckQualityGroup', 1);

-- 8. Add initial authorities to roles
--
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'AdminGroup'), id FROM authority WHERE title LIKE '%';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckDossierGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckDossierGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckDossierGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckDossierGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'PrepareDossierGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'PrepareDossierGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessPagination_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierAGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierAGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ScanDossierAGroup'), id FROM authority WHERE title LIKE '%ProcessImages_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierBGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierBGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ScanDossierBGroup'), id FROM authority WHERE title LIKE '%ProcessImages_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessPagination_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewProcessImages_clientAssignable'));
