package com.ddorong.ddorong_bot_be.settings.dto;

public record AddChannelRequest(
        String channelType,  // SLACK, DISCORD, EMAIL
        String destination   // Webhook URL 또는 이메일 주소
) {
}