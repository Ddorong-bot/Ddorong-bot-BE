package com.ddorong.ddorong_bot_be.delivery.channel;

public class ChannelSendException extends Exception {
    
    public ChannelSendException(String message) {
        super(message);
    }
    
    public ChannelSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
