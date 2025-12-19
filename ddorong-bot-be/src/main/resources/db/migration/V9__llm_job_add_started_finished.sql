ALTER TABLE llm_job
    ADD COLUMN IF NOT EXISTS started_at timestamptz;

ALTER TABLE llm_job
    ADD COLUMN IF NOT EXISTS finished_at timestamptz;
