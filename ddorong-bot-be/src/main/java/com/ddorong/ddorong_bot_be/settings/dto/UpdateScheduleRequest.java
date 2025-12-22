package com.ddorong.ddorong_bot_be.settings.dto;

public record UpdateScheduleRequest(
        String deliveryTime,    // "09:00" 형식
        Boolean isEnabled
) {
}
