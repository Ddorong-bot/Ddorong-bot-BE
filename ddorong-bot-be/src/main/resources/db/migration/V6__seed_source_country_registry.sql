-- =========================
-- Source → Country registry (final)
-- =========================

-- =========================
-- Japan (JP)
-- =========================
INSERT INTO source_country_registry(source_name, country_code, confidence) VALUES
  ('아사히 신문', 'JP', 100),
  ('아사히신문', 'JP', 100),
  ('Asahi Shimbun', 'JP', 100),
  ('The Asahi Shimbun', 'JP', 100),

  ('재팬 타임즈', 'JP', 100),
  ('Japan Times', 'JP', 100),
  ('The Japan Times', 'JP', 100),

  ('Japan Today', 'JP', 100),
  ('SoraNews', 'JP', 100),
  ('SoraNews24', 'JP', 100),

  ('News On Japan', 'JP', 100),
  ('NHK', 'JP', 100),
  ('NHK World', 'JP', 100),
  ('NHK WORLD-JAPAN', 'JP', 100),

-- =========================
-- China (CN)
-- =========================
  ('신화사', 'CN', 100),
  ('신화 통신', 'CN', 100),
  ('Xinhua', 'CN', 100),
  ('Xinhua News Agency', 'CN', 100),
  ('新华社', 'CN', 100),

  ('중국통신사', 'CN', 100),
  ('중국 통신사', 'CN', 100),

  ('인민일보', 'CN', 100),
  ('People''s Daily', 'CN', 100),
  ('人民日报', 'CN', 100),

-- =========================
-- Arab world
-- =========================
  -- Qatar
  ('Al Jazeera', 'QA', 100),
  ('알자지라', 'QA', 100),
  ('الجزيرة', 'QA', 100),

  -- UAE
  ('Gulf News', 'AE', 100),
  ('걸프 뉴스', 'AE', 100),

  ('Sky News Arabia', 'AE', 100),
  ('스카이 뉴스 아라비아', 'AE', 100),

  -- BBC Arabic (아랍권 서비스로 매핑)
  ('BBC Arabic', 'AE', 100),
  ('BBC 아랍어판', 'AE', 100),
  ('بي بي سي عربي', 'AE', 100),

  -- Saudi Arabia
  ('Al Arabiya', 'SA', 100),
  ('알 아라비아', 'SA', 100),
  ('العربية', 'SA', 100),

  ('Asharq Al-Awsat', 'SA', 100),
  ('الشرق الأوسط', 'SA', 100),

  -- Palestine
  ('Al-Quds', 'PS', 100),
  ('알쿠드스', 'PS', 100),
  ('القدس', 'PS', 100)

ON CONFLICT (source_name) DO NOTHING;
