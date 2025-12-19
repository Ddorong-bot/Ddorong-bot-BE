CREATE TABLE IF NOT EXISTS source_country_registry (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source_name VARCHAR(200) NOT NULL UNIQUE,   -- "BBC", "Reuters"
  country_code VARCHAR(10) NOT NULL,          -- "GB", "US"
  confidence SMALLINT NOT NULL DEFAULT 100,   -- 0~100 (선택)
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
