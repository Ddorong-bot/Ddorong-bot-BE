package com.ddorong.ddorong_bot_be.llm;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ddorong.ddorong_bot_be.llm.dto.LlmPullResponse;
import com.ddorong.ddorong_bot_be.llm.dto.LlmResultUpsertRequest;
import com.ddorong.ddorong_bot_be.llm.dto.LlmResultUpsertResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    /**
     * 번역/요약 팀(워크커)이 처리할 기사 목록을 가져갑니다.
     * - 동시 작업자를 고려해 DB에서 SKIP LOCKED로 "작업 선점" 처리합니다.
     */
    @GetMapping("/pull")
    public ResponseEntity<LlmPullResponse> pullWorkItems(
            @RequestParam(defaultValue = "ko") @NotBlank String languageTarget,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(llmService.pullWorkItems(languageTarget, limit));
    }

    /**
     * 번역/요약 결과를 저장합니다.
     * - localized는 upsert
     * - summary는 값이 있으면 upsert
     */
    @PostMapping("/results")
    public ResponseEntity<LlmResultUpsertResponse> upsertResult(@RequestBody @Valid LlmResultUpsertRequest req) {
        return ResponseEntity.ok(llmService.upsertResult(req));
    }
}
