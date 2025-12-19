package com.ddorong.ddorong_bot_be.admin;

import com.ddorong.ddorong_bot_be.admin.dto.IngestionBulkRequest;
import com.ddorong.ddorong_bot_be.admin.dto.IngestionBulkResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ingestion")
public class IngestionAdminController {

    private final IngestionService ingestionService;

    public IngestionAdminController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/articles:bulk")
    public ResponseEntity<IngestionBulkResponse> bulk(@Valid @RequestBody IngestionBulkRequest req) {
        return ResponseEntity.ok(ingestionService.ingestBulk(req));
    }
}
