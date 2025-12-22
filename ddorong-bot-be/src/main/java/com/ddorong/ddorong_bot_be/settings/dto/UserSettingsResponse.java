package com.ddorong.ddorong_bot_be.settings.dto;

import java.util.List;
import java.util.UUID;

public record UserSettingsResponse(
        UUID id,
        String code,
        String displayName,
        String timezone,
        PreferenceDto preference,
        List<InterestDto> interests,
        List<ChannelDto> channels
) {
}
