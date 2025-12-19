package com.ddorong.ddorong_bot_be.admin.dto;

import java.util.UUID;

public record IngestionBulkResponse(
        UUID ingestionRunId,
        int received,
        int inserted,
        int duplicated
) {}
