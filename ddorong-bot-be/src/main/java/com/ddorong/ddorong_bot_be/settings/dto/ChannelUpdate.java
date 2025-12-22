package com.ddorong.ddorong_bot_be.settings.dto;

import java.util.UUID;

public record ChannelUpdate(
        UUID id,
        String destination,
        Boolean isEnabled
) {
}
