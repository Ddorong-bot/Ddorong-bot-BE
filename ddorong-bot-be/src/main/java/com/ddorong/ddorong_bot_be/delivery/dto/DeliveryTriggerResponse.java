package com.ddorong.ddorong_bot_be.delivery.dto;

import java.util.UUID;

/**
 * 발송 트리거 응답
 */
public record DeliveryTriggerResponse(
    UUID deliveryRunId,
    String userCode,
    int articlesMatched,
    int channelsSent,
    int channelsFailed,
    String status,
    Boolean dryRun
) {}
