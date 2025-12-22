package com.ddorong.ddorong_bot_be.delivery.channel;

import com.ddorong.ddorong_bot_be.delivery.dto.NewsDigest;
import com.ddorong.ddorong_bot_be.delivery.dto.NewsItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(SlackChannelSender.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SlackChannelSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getChannelType() {
        return "SLACK";
    }

    @Override
    public void send(String destination, NewsDigest digest) throws ChannelSendException {
        try {
            String payload = buildSlackPayload(digest);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destination))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ChannelSendException(
                    "Slack webhook failed: " + response.statusCode() + " - " + response.body()
                );
            }

            log.info("Slack message sent successfully to {}", maskWebhook(destination));
            
        } catch (ChannelSendException e) {
            throw e;
        } catch (Exception e) {
            throw new ChannelSendException("Failed to send Slack message", e);
        }
    }

    private String buildSlackPayload(NewsDigest digest) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        
        // 헤더 블록
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(Map.of(
            "type", "header",
            "text", Map.of(
                "type", "plain_text",
                "text", "📰 " + digest.getTitle()
            )
        ));

        // 각 뉴스 아이템
        for (NewsItemDto item : digest.getItems()) {
            blocks.add(Map.of(
                "type", "section",
                "text", Map.of(
                    "type", "mrkdwn",
                    "text", formatNewsItem(item)
                )
            ));
            blocks.add(Map.of("type", "divider"));
        }

        payload.put("blocks", blocks);
        return objectMapper.writeValueAsString(payload);
    }

    private String formatNewsItem(NewsItemDto item) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(item.getTranslatedTitle()).append("*\n");
        sb.append(item.getSummary()).append("\n");
        sb.append("<").append(item.getOriginalUrl()).append("|원문 보기>");
        return sb.toString();
    }

    private String maskWebhook(String url) {
        if (url == null || url.length() < 30) {
            return "***";
        }
        return url.substring(0, 30) + "...";
    }
}
