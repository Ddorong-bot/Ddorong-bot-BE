-- Enable extensions if needed (uuid generation)
-- (PostgreSQL 13+ often uses gen_random_uuid via pgcrypto)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- 1) Master tables
-- =========================
CREATE TABLE IF NOT EXISTS news_source (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(200) NOT NULL,
  type VARCHAR(30) NOT NULL, -- RSS, API, SCRAPE
  base_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS news_category (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(50) NOT NULL UNIQUE, -- world, tech, business...
  display_name VARCHAR(100) NOT NULL
);

-- =========================
-- 2) Test users (no login yet)
-- =========================
CREATE TABLE IF NOT EXISTS app_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(50) NOT NULL UNIQUE, -- user_A, user_B, user_C
  display_name VARCHAR(100) NOT NULL,
  timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_preference (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  language_target VARCHAR(10) NOT NULL DEFAULT 'ko',
  digest_size INT NOT NULL DEFAULT 10,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(user_id)
);

CREATE TABLE IF NOT EXISTS user_interest (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  type VARCHAR(20) NOT NULL, -- CATEGORY, KEYWORD, SOURCE
  value VARCHAR(200) NOT NULL,
  is_include BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_user_interest_user ON user_interest(user_id);

CREATE TABLE IF NOT EXISTS user_channel (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  channel_type VARCHAR(20) NOT NULL, -- EMAIL, SLACK, DISCORD
  destination VARCHAR(500) NOT NULL, -- email/webhook url
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_user_channel_user ON user_channel(user_id);

CREATE TABLE IF NOT EXISTS user_delivery_schedule (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  cron_expr VARCHAR(100) NOT NULL,
  next_run_at TIMESTAMPTZ,
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_schedule_due ON user_delivery_schedule(next_run_at) WHERE next_run_at IS NOT NULL;

-- =========================
-- 3) Articles + assets + enrichment results
-- =========================
CREATE TABLE IF NOT EXISTS news_article (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source_id UUID REFERENCES news_source(id),
  category_id UUID REFERENCES news_category(id),
  external_id VARCHAR(200),
  url TEXT NOT NULL,
  title TEXT NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(200),
  published_at TIMESTAMPTZ,
  fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  content_hash CHAR(64) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'RAW', -- RAW, ENRICHED, FAILED
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(content_hash)
);
CREATE INDEX IF NOT EXISTS idx_article_published ON news_article(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_article_category_published ON news_article(category_id, published_at DESC);

CREATE TABLE IF NOT EXISTS news_asset (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  article_id UUID NOT NULL REFERENCES news_article(id) ON DELETE CASCADE,
  asset_type VARCHAR(20) NOT NULL, -- THUMBNAIL, IMAGE
  s3_bucket VARCHAR(200),
  s3_key TEXT,
  public_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_asset_article ON news_asset(article_id);

CREATE TABLE IF NOT EXISTS news_article_localized (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  article_id UUID NOT NULL REFERENCES news_article(id) ON DELETE CASCADE,
  lang VARCHAR(10) NOT NULL, -- ko
  title_translated TEXT NOT NULL,
  content_translated TEXT,
  provider VARCHAR(20) NOT NULL, -- OPENAI, GEMINI
  model VARCHAR(100),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(article_id, lang)
);

CREATE TABLE IF NOT EXISTS news_article_summary (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  article_id UUID NOT NULL REFERENCES news_article(id) ON DELETE CASCADE,
  lang VARCHAR(10) NOT NULL,
  summary TEXT NOT NULL,
  bullets JSONB,
  provider VARCHAR(20) NOT NULL,
  model VARCHAR(100),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(article_id, lang)
);

-- =========================
-- 4) Runs / Jobs / Delivery logs
-- =========================
CREATE TABLE IF NOT EXISTS ingestion_run (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ,
  trigger VARCHAR(20) NOT NULL, -- SCHEDULED, MANUAL
  status VARCHAR(20) NOT NULL, -- SUCCESS, PARTIAL, FAILED
  message TEXT
);

CREATE TABLE IF NOT EXISTS llm_job (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  article_id UUID NOT NULL REFERENCES news_article(id) ON DELETE CASCADE,
  job_type VARCHAR(50) NOT NULL, -- TRANSLATE_SUMMARIZE
  target_lang VARCHAR(10) NOT NULL DEFAULT 'ko',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
  attempt_count INT NOT NULL DEFAULT 0,
  next_retry_at TIMESTAMPTZ,
  last_error TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_llm_job_status ON llm_job(status, next_retry_at);

CREATE TABLE IF NOT EXISTS delivery_run (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  scheduled_at TIMESTAMPTZ NOT NULL,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ,
  status VARCHAR(20) NOT NULL, -- SUCCESS, PARTIAL, FAILED
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS delivery_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  delivery_run_id UUID NOT NULL REFERENCES delivery_run(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  article_id UUID NOT NULL REFERENCES news_article(id) ON DELETE CASCADE,
  channel_type VARCHAR(20) NOT NULL,
  destination VARCHAR(500) NOT NULL,
  status VARCHAR(30) NOT NULL, -- SENT, FAILED, SKIPPED_DUPLICATE, SKIPPED_NO_ENRICHED
  error_message TEXT,
  sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(user_id, article_id, channel_type)
);
CREATE INDEX IF NOT EXISTS idx_delivery_user_sent ON delivery_log(user_id, sent_at DESC);
