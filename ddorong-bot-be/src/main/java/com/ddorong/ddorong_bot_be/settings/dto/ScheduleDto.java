package com.ddorong.ddorong_bot_be.settings.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleDto(
        UUID id,
        String cronExpr,
        String deliveryTime,    // "09:00" 형식
        OffsetDateTime nextRunAt,
        Boolean isEnabled
) {
    /**
     * cron 표현식에서 시간 추출 (예: "0 0 9 * * *" -> "09:00")
     */
    public static String cronToTime(String cronExpr) {
        if (cronExpr == null || cronExpr.isEmpty()) {
            return "09:00";
        }
        try {
            String[] parts = cronExpr.split(" ");
            // 형식: 초 분 시 일 월 요일
            int minute = Integer.parseInt(parts[1]);
            int hour = Integer.parseInt(parts[2]);
            return String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            return "09:00";
        }
    }

    /**
     * 시간을 cron 표현식으로 변환 (예: "09:00" -> "0 0 9 * * *")
     */
    public static String timeToCron(String time) {
        if (time == null || time.isEmpty()) {
            return "0 0 9 * * *";
        }
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return String.format("0 %d %d * * *", minute, hour);
        } catch (Exception e) {
            return "0 0 9 * * *";
        }
    }
}
