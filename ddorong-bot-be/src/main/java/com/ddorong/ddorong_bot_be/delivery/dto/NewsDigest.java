package com.ddorong.ddorong_bot_be.delivery.dto;

import java.util.List;

/**
 * 사용자에게 전송될 뉴스 다이제스트
 */
public class NewsDigest {
    
    private String title;
    private String userCode;
    private List<NewsItemDto> items;
    
    public NewsDigest(String title, String userCode, List<NewsItemDto> items) {
        this.title = title;
        this.userCode = userCode;
        this.items = items;
    }
    
    public String getTitle() { return title; }
    public String getUserCode() { return userCode; }
    public List<NewsItemDto> items() { return items; }
    public List<NewsItemDto> getItems() { return items; }
}
