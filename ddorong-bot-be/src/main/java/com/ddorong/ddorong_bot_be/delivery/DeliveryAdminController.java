package com.ddorong.ddorong_bot_be.delivery;

import com.ddorong.ddorong_bot_be.delivery.dto.DeliveryTriggerRequest;
import com.ddorong.ddorong_bot_be.delivery.dto.DeliveryTriggerResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 전송 관련 관리자 API
 */
@RestController
@RequestMapping("/api/admin/delivery")
public class DeliveryAdminController {

    private final DeliveryService deliveryService;

    public DeliveryAdminController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * 특정 사용자에게 즉시 발송 (테스트용)
     * 
     * POST /api/admin/delivery/run
     * {
     *   "userCode": "user_A",
     *   "channelType": "SLACK",  // optional
     *   "dryRun": false
     * }
     */
    @PostMapping("/run")
    public ResponseEntity<DeliveryTriggerResponse> triggerDelivery(
            @Valid @RequestBody DeliveryTriggerRequest request
    ) {
        DeliveryTriggerResponse response = deliveryService.deliverToUser(request);
        return ResponseEntity.ok(response);
    }
}
