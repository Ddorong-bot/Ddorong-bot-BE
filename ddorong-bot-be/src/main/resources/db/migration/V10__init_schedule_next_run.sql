-- Initialize next_run_at for existing schedules
-- This calculates the next run time based on current time and cron expression

-- For user_A (09:00 daily)
UPDATE user_delivery_schedule
SET next_run_at = (
    CASE 
        WHEN EXTRACT(HOUR FROM NOW() AT TIME ZONE 'Asia/Seoul') < 9 
        THEN DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '9 hours'
        ELSE DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '1 day' + INTERVAL '9 hours'
    END
)
WHERE user_id IN (SELECT id FROM app_user WHERE code = 'user_A')
  AND cron_expr = '0 0 9 * * *'
  AND next_run_at IS NULL;

-- For user_B (12:30 daily)
UPDATE user_delivery_schedule
SET next_run_at = (
    CASE 
        WHEN EXTRACT(HOUR FROM NOW() AT TIME ZONE 'Asia/Seoul') < 12 
             OR (EXTRACT(HOUR FROM NOW() AT TIME ZONE 'Asia/Seoul') = 12 
                 AND EXTRACT(MINUTE FROM NOW() AT TIME ZONE 'Asia/Seoul') < 30)
        THEN DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '12 hours 30 minutes'
        ELSE DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '1 day' + INTERVAL '12 hours 30 minutes'
    END
)
WHERE user_id IN (SELECT id FROM app_user WHERE code = 'user_B')
  AND cron_expr = '0 30 12 * * *'
  AND next_run_at IS NULL;

-- For user_C (18:00 daily)
UPDATE user_delivery_schedule
SET next_run_at = (
    CASE 
        WHEN EXTRACT(HOUR FROM NOW() AT TIME ZONE 'Asia/Seoul') < 18 
        THEN DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '18 hours'
        ELSE DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Seoul') + INTERVAL '1 day' + INTERVAL '18 hours'
    END
)
WHERE user_id IN (SELECT id FROM app_user WHERE code = 'user_C')
  AND cron_expr = '0 0 18 * * *'
  AND next_run_at IS NULL;
