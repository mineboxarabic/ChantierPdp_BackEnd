package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.ActivityLogDTO;
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsDTO; // Create this
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Map;

public interface ActivityLogService {

    void logActivity(String actorUsername, String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details);

    void logActivity(User actor, String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details);

    @Transactional
    void logActivity(String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details);

    Page<ActivityLogDTO> getActivityLogsForTarget(Long targetEntityId, String targetEntityType, Pageable pageable);

    Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable);

    MonthlyActivityStatsDTO getMonthlyDashboardStats(YearMonth month);

    User getCurrentActor();
}