package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.ActivityLog;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsDTO; // You'll create this DTO
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepo extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByTargetEntityIdAndTargetEntityTypeOrderByTimestampDesc(Long targetEntityId, String targetEntityType, Pageable pageable);

    Page<ActivityLog> findByActorOrderByTimestampDesc(User actor, Pageable pageable);

    Page<ActivityLog> findByActionKeyOrderByTimestampDesc(String actionKey, Pageable pageable);

    Page<ActivityLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Example for a more complex aggregation for the dashboard
    // Note: Directly mapping to a DTO constructor. Ensure MonthlyActivityStatsDTO has a matching constructor.
    // This is a simplified example; you might need more specific counts or a different approach.

    // Simpler count example
    @Query("SELECT COUNT(al.id) FROM ActivityLog al WHERE al.actionKey = :actionKey AND al.timestamp >= :startDate AND al.timestamp < :endDate")
    Long countByActionKeyAndTimestampBetween(@Param("actionKey") String actionKey, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long count();

    @Query(value = "SELECT " +
            "TO_CHAR(al.timestamp, 'YYYY-MM') AS month, " +
            "CAST(COALESCE(SUM(CASE WHEN al.action_key = 'chantier.created' THEN 1 ELSE 0 END), 0) AS INTEGER) AS chantiersCreated, " +
            "CAST(COALESCE(SUM(CASE WHEN al.action_key = 'chantier.status.changed' AND al.description LIKE '%vers COMPLETED%' THEN 1 ELSE 0 END), 0) AS INTEGER) AS chantiersCompleted, " + // Fallback if JSON query is hard
            "CAST(COALESCE(SUM(CASE WHEN al.action_key = 'chantier.pdp_pending_detected' THEN 1 ELSE 0 END), 0) AS INTEGER) AS chantiersWithPdpPending, " +
            "CAST(COALESCE(SUM(CASE WHEN al.action_key = 'chantier.bdt_pending_detected' THEN 1 ELSE 0 END), 0) AS INTEGER) AS chantiersWithBdtPending, " +
            "CAST(COALESCE(SUM(CASE WHEN al.action_key LIKE '%.signed' THEN 1 ELSE 0 END), 0) AS INTEGER) AS documentsSigned, " +
            "CAST(0 AS INTEGER) AS actionsRequiredOnDocuments " +
            "FROM activity_log al " + // Use actual table name (snake_case by default)
            "WHERE TO_CHAR(al.timestamp, 'YYYY-MM') = :monthStr " +
            "GROUP BY TO_CHAR(al.timestamp, 'YYYY-MM')",
            nativeQuery = true)
    MonthlyActivityStatsProjection getAggregatedMonthlyStatsNative(@Param("monthStr") String monthStr);
}