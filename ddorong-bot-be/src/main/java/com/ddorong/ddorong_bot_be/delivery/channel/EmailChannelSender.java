package com.ddorong.ddorong_bot_be.delivery.channel;

import com.ddorong.ddorong_bot_be.delivery.dto.NewsDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Email 채널 발송 구현체
 * TODO: JavaMailSender 설정 후 구현
 */
@Component
public class EmailChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(EmailChannelSender.class);

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public void send(String destination, NewsDigest digest) throws ChannelSendException {
        log.warn("Email channel is not implemented yet. Would send to: {}", destination);
        // TODO: Implement email sending with JavaMailSender
        throw new ChannelSendException("Email channel not implemented yet");
    }
}
