-- Categories
INSERT INTO news_category(code, display_name) VALUES
  ('tech', 'Technology'),
  ('science', 'Science'),
  ('politics', 'Politics'), 
  ('economy', 'Economy'), 
  ('others', 'Others')
ON CONFLICT (code) DO NOTHING;

-- Sources (example)
INSERT INTO news_source(name, type, base_url) VALUES
  ('BBC', 'RSS', 'https://www.bbc.co.uk'),
  ('Reuters', 'RSS', 'https://www.reuters.com')
ON CONFLICT (name) DO NOTHING;

-- Users
INSERT INTO app_user(code, display_name, timezone) VALUES
  ('user_A', 'User A', 'Asia/Seoul'),
  ('user_B', 'User B', 'Asia/Seoul'),
  ('user_C', 'User C', 'Asia/Seoul')
ON CONFLICT (code) DO NOTHING;

-- Preferences
INSERT INTO user_preference(user_id, language_target, digest_size)
SELECT id, 'ko', 10 FROM app_user
ON CONFLICT (user_id) DO NOTHING;

-- Interests (example)
-- A: tech + keyword AI
INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'CATEGORY', 'tech', true FROM app_user WHERE code='user_A'
ON CONFLICT DO NOTHING;

INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'KEYWORD', 'AI', true FROM app_user WHERE code='user_A'
ON CONFLICT DO NOTHING;

-- B: world + keyword economy
INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'CATEGORY', 'world', true FROM app_user WHERE code='user_B'
ON CONFLICT DO NOTHING;

INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'KEYWORD', 'economy', true FROM app_user WHERE code='user_B'
ON CONFLICT DO NOTHING;

-- C: science + keyword quantum
INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'CATEGORY', 'science', true FROM app_user WHERE code='user_C'
ON CONFLICT DO NOTHING;

INSERT INTO user_interest(user_id, type, value, is_include)
SELECT id, 'KEYWORD', 'quantum', true FROM app_user WHERE code='user_C'
ON CONFLICT DO NOTHING;

-- Channels (destination은 임시값; 나중에 UI에서 수정)
INSERT INTO user_channel(user_id, channel_type, destination, is_enabled)
SELECT id, 'EMAIL', 'test-a@example.com', true FROM app_user WHERE code='user_A'
ON CONFLICT DO NOTHING;

INSERT INTO user_channel(user_id, channel_type, destination, is_enabled)
SELECT id, 'SLACK', 'https://hooks.slack.com/services/REPLACE/ME', false FROM app_user WHERE code='user_B'
ON CONFLICT DO NOTHING;

INSERT INTO user_channel(user_id, channel_type, destination, is_enabled)
SELECT id, 'DISCORD', 'https://discord.com/api/webhooks/REPLACE/ME', false FROM app_user WHERE code='user_C'
ON CONFLICT DO NOTHING;

-- Schedules (cron 예시: 매일 09:00 / 12:30 / 18:00)
INSERT INTO user_delivery_schedule(user_id, cron_expr, next_run_at, is_enabled)
SELECT id, '0 0 9 * * *', null, true FROM app_user WHERE code='user_A'
ON CONFLICT DO NOTHING;

INSERT INTO user_delivery_schedule(user_id, cron_expr, next_run_at, is_enabled)
SELECT id, '0 30 12 * * *', null, true FROM app_user WHERE code='user_B'
ON CONFLICT DO NOTHING;

INSERT INTO user_delivery_schedule(user_id, cron_expr, next_run_at, is_enabled)
SELECT id, '0 0 18 * * *', null, true FROM app_user WHERE code='user_C'
ON CONFLICT DO NOTHING;
