package com.ddorong.ddorong_bot_be.delivery.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 관리자용 강제 발송 트리거 요청
 */
public record DeliveryTriggerRequest(
    @NotBlank String userCode,
    String channelType,
    Boolean dryRun
) {
    public DeliveryTriggerRequest {
        if (dryRun == null) {
            dryRun = false;
        }
    }
}
