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
public class DiscordChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(DiscordChannelSender.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DiscordChannelSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getChannelType() {
        return "DISCORD";
    }

    @Override
    public void send(String destination, NewsDigest digest) throws ChannelSendException {
        try {
            String payload = buildDiscordPayload(digest);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destination))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ChannelSendException(
                    "Discord webhook failed: " + response.statusCode() + " - " + response.body()
                );
            }

            log.info("Discord message sent successfully to {}", maskWebhook(destination));
            
        } catch (ChannelSendException e) {
            throw e;
        } catch (Exception e) {
            throw new ChannelSendException("Failed to send Discord message", e);
        }
    }

    private String buildDiscordPayload(NewsDigest digest) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", "📰 **" + digest.getTitle() + "**");
        
        List<Map<String, Object>> embeds = new ArrayList<>();
        
        for (NewsItemDto item : digest.getItems()) {
            Map<String, Object> embed = new HashMap<>();
            embed.put("title", item.getTranslatedTitle());
            embed.put("description", truncate(item.getSummary(), 300));
            embed.put("url", item.getOriginalUrl());
            embed.put("color", 0x5865F2); // Discord 블루
            
            embeds.add(embed);
            
            // Discord는 한 번에 최대 10개 embeds만 허용
            if (embeds.size() >= 10) {
                break;
            }
        }
        
        payload.put("embeds", embeds);
        return objectMapper.writeValueAsString(payload);
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String maskWebhook(String url) {
        if (url == null || url.length() < 30) {
            return "***";
        }
        return url.substring(0, 30) + "...";
    }
}
