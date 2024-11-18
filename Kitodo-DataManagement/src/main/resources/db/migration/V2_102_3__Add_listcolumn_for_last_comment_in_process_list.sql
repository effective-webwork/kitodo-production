-- Insert list column 'process.lastComment'.
INSERT IGNORE INTO listColumn (title) VALUES ('process.lastComment');

-- Update title of 'task.correctionComment' list column.
UPDATE listColumn SET title = 'task.lastComment' WHERE title = 'task.correctionComment';
