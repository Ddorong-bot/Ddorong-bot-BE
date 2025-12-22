package com.ddorong.ddorong_bot_be.settings.dto;

import com.ddorong.ddorong_bot_be.domain.UserChannel;

import java.util.UUID;

public record ChannelDto(
        UUID id,
        String channelType,
        String destination,
        Boolean isEnabled
) {
    public static ChannelDto from(UserChannel channel) {
        return new ChannelDto(
                channel.getId(),
                channel.getChannelType(),
                channel.getDestination(),
                channel.getIsEnabled()
        );
    }
}
