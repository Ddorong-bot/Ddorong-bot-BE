package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.LlmJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LlmJobRepository extends JpaRepository<LlmJob, UUID> {

    @Query(value = """
        SELECT *
        FROM llm_job
        WHERE status = 'PENDING'
        ORDER BY created_at ASC
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
        """, nativeQuery = true)
    List<LlmJob> lockPendingJobs(@Param("limit") int limit);
}
