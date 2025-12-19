package com.ddorong.ddorong_bot_be.llmjob;

import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobCompleteRequest;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobFailRequest;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobPullResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/llm")
public class LlmAdminController {

    private final LlmJobService llmJobService;

    public LlmAdminController(LlmJobService llmJobService) {
        this.llmJobService = llmJobService;
    }

    @GetMapping("/jobs:pull")
    public ResponseEntity<LlmJobPullResponse> pull(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(llmJobService.pull(limit));
    }

    @PostMapping("/jobs/{jobId}:complete")
    public ResponseEntity<?> complete(@PathVariable UUID jobId, @RequestBody LlmJobCompleteRequest req) {
        llmJobService.complete(jobId, req);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/jobs/{jobId}:fail")
    public ResponseEntity<?> fail(@PathVariable UUID jobId, @RequestBody LlmJobFailRequest req) {
        llmJobService.fail(jobId, req);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
