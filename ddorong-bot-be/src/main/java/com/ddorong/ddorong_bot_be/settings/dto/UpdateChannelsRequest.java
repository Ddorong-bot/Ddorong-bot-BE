package com.ddorong.ddorong_bot_be.settings.dto;

import java.util.List;

public record UpdateChannelsRequest(
        List<ChannelUpdate> channels
) {
}
