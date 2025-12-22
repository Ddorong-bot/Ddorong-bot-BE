package com.ddorong.ddorong_bot_be.delivery.channel;

import com.ddorong.ddorong_bot_be.delivery.dto.NewsDigest;

/**
 * 채널별 메시지 발송 인터페이스
 */
public interface ChannelSender {
    
    /**
     * 지원하는 채널 타입
     */
    String getChannelType();
    
    /**
     * 뉴스 다이제스트 발송
     * 
     * @param destination 발송 대상 (webhook URL, email 등)
     * @param digest 뉴스 다이제스트
     * @throws ChannelSendException 발송 실패 시
     */
    void send(String destination, NewsDigest digest) throws ChannelSendException;
}
