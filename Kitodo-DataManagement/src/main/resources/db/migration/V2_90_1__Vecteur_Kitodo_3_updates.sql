-- 1. Add new authorities to cancel jobs
INSERT INTO authority (title) VALUES ('cancelJob_globalAssignable');

SET SQL_SAFE_UPDATES=0;

-- 2. Set 'separateStructure=true' for all tasks
UPDATE task SET separateStructure = 1 WHERE TRUE;

-- 3. Set 'repeatOnCorrection=true' for all tasks
UPDATE task SET repeatOnCorrection = 1 WHERE TRUE;

SET SQL_SAFE_UPDATES=1;
