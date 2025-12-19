ALTER TABLE llm_job
    ADD COLUMN IF NOT EXISTS error_code varchar(100);

ALTER TABLE llm_job
    ADD COLUMN IF NOT EXISTS error_message text;
