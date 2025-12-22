package com.ddorong.ddorong_bot_be.settings.dto;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String code,
        String displayName,
        String timezone
) {
}
