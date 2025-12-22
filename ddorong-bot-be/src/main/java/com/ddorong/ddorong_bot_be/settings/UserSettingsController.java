package com.ddorong.ddorong_bot_be.settings;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ddorong.ddorong_bot_be.settings.dto.AddChannelRequest;
import com.ddorong.ddorong_bot_be.settings.dto.ChannelDto;
import com.ddorong.ddorong_bot_be.settings.dto.ScheduleDto;
import com.ddorong.ddorong_bot_be.settings.dto.TestMessageResponse;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateChannelsRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateInterestsRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdatePreferencesRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateScheduleRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UserListResponse;
import com.ddorong.ddorong_bot_be.settings.dto.UserSettingsResponse;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    /**
     * 모든 사용자 목록 조회
     */
    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        return ResponseEntity.ok(userSettingsService.getAllUsers());
    }

    /**
     * 특정 사용자 설정 조회
     */
    @GetMapping("/{userCode}")
    public ResponseEntity<UserSettingsResponse> getUserSettings(@PathVariable String userCode) {
        return ResponseEntity.ok(userSettingsService.getUserSettings(userCode));
    }

    /**
     * 기본 설정 업데이트
     */
    @PutMapping("/{userCode}/preferences")
    public ResponseEntity<Void> updatePreferences(
            @PathVariable String userCode,
            @RequestBody UpdatePreferencesRequest request) {
        userSettingsService.updatePreferences(userCode, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 채널 업데이트 (활성화/비활성화, destination 변경)
     */
    @PutMapping("/{userCode}/channels")
    public ResponseEntity<Void> updateChannels(
            @PathVariable String userCode,
            @RequestBody UpdateChannelsRequest request) {
        userSettingsService.updateChannels(userCode, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 채널 추가
     */
    @PostMapping("/{userCode}/channels")
    public ResponseEntity<ChannelDto> addChannel(
            @PathVariable String userCode,
            @RequestBody AddChannelRequest request) {
        ChannelDto channel = userSettingsService.addChannel(userCode, request);
        return ResponseEntity.ok(channel);
    }

    /**
     * 채널 삭제
     */
    @DeleteMapping("/{userCode}/channels/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable String userCode,
            @PathVariable UUID channelId) {
        userSettingsService.deleteChannel(userCode, channelId);
        return ResponseEntity.ok().build();
    }

    /**
     * 채널 테스트 메시지 전송
     */
    @PostMapping("/{userCode}/channels/{channelId}/test")
    public ResponseEntity<TestMessageResponse> testChannel(
            @PathVariable String userCode,
            @PathVariable UUID channelId) {
        TestMessageResponse response = userSettingsService.sendTestMessage(userCode, channelId);
        return ResponseEntity.ok(response);
    }

    /**
     * 관심사 업데이트
     */
    @PutMapping("/{userCode}/interests")
    public ResponseEntity<Void> updateInterests(
            @PathVariable String userCode,
            @RequestBody UpdateInterestsRequest request) {
        userSettingsService.updateInterests(userCode, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userCode}/schedule")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable String userCode,
            @RequestBody UpdateScheduleRequest request) {
        ScheduleDto schedule = userSettingsService.updateSchedule(userCode, request);
        return ResponseEntity.ok(schedule);
    }
}