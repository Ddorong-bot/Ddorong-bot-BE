ALTER TABLE news_source
ADD COLUMN IF NOT EXISTS country_code VARCHAR(10);

CREATE INDEX IF NOT EXISTS idx_news_source_country_code
ON news_source(country_code);
