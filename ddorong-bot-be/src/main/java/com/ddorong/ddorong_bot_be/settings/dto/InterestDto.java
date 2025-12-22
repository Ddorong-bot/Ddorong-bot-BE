package com.ddorong.ddorong_bot_be.settings.dto;

import com.ddorong.ddorong_bot_be.domain.UserInterest;

import java.util.UUID;

public record InterestDto(
        UUID id,
        String type,
        String value,
        Boolean isInclude
) {
    public static InterestDto from(UserInterest interest) {
        return new InterestDto(
                interest.getId(),
                interest.getType(),
                interest.getValue(),
                interest.getIsInclude()
        );
    }
}
