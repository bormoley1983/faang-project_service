ALTER TABLE meet
ADD COLUMN google_event_id VARCHAR(255);

ALTER TABLE schedule
ADD COLUMN google_event_id VARCHAR(255);

ALTER TABLE project
ADD COLUMN google_calendar_id VARCHAR(255);