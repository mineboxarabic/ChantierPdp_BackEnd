package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ActivityLogRepo;
import com.danone.pdpbackend.Repo.ChantierRepo;
import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Services.ActivityLogService;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.ActivityLog;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.ActivityLogDTO;
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsDTO;
import com.danone.pdpbackend.Utils.mappers.ActivityLogMapper; // You'll create this mapper
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepo activityLogRepo;
    private final UsersRepo usersRepo;
    private final ChantierRepo chantierRepo; // Autowire ChantierRepo
    private final DocumentRepo documentRepo; // Autowire DocumentRepo
    private final ActivityLogMapper activityLogMapper;


    public User getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return usersRepo.findByUsername(username).orElse(null);
        }
        return null;
    }

    private String getTargetEntityDescription(Long entityId, String entityType) {
        if (entityId == null || entityType == null) {
            return null;
        }
        try {
            switch (entityType.toUpperCase()) {
                case "CHANTIER":
                    return chantierRepo.findById(entityId).map(c -> "Chantier: " + c.getNom()).orElse(null);
                case "PDP":
                case "BDT":
                    return documentRepo.findById(entityId).map(d -> entityType + ": ID " + d.getId() + (d.getChantier() != null ? " (Chantier: " + d.getChantier().getNom() + ")" : "")).orElse(null);
                case "USER":
                    return usersRepo.findById(entityId).map(u -> "User: " + u.getUsername()).orElse(null);
                case "WORKER":
                    // Assuming you have a WorkerRepo, similar logic
                    // return workerRepo.findById(entityId).map(w -> "Worker: " + w.getNom()).orElse(null);
                default:
                    return entityType + " ID: " + entityId;
            }
        } catch (Exception e) {
            log.warn("Could not fetch description for {} ID {}", entityType, entityId, e);
            return entityType + " ID: " + entityId;
        }
    }


    @Override
    @Transactional
    public void logActivity(String actorUsername, String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details) {
        User actor = null;
        if (actorUsername != null && !"system".equalsIgnoreCase(actorUsername)) {
            actor = usersRepo.findByUsername(actorUsername)
                    .orElseGet(() -> {
                        log.warn("Actor user with username '{}' not found for activity log.", actorUsername);
                        return null; // Log as system or handle error
                    });
        }
        logInternal(actor, actionKey, targetEntityId, targetEntityType, description, details);
    }

    @Override
    @Transactional
    public void logActivity(User actor, String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details) {
        logInternal(actor, actionKey, targetEntityId, targetEntityType, description, details);
    }

    @Override
    @Transactional
    public void logActivity(String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details) {
        User actor = getCurrentActor();
        if(actor != null && actor.getUsername() != null) {
            logInternal(actor, actionKey, targetEntityId, targetEntityType, description, details);
        }


    }


    private void logInternal(User actor, String actionKey, Long targetEntityId, String targetEntityType, String description, Map<String, Object> details) {
        ActivityLog logEntry = ActivityLog.builder()
                .actor(actor)
                .actionKey(actionKey)
                .targetEntityId(targetEntityId)
                .targetEntityType(targetEntityType)
                .description(description)
                .details(details)
                .build(); // Timestamp is set by @PrePersist
        activityLogRepo.save(logEntry);
        log.debug("Activity logged: Actor='{}', Action='{}', Target='{}:{}', Desc='{}'",
                actor != null ? actor.getUsername() : "system", actionKey, targetEntityType, targetEntityId, description);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsForTarget(Long targetEntityId, String targetEntityType, Pageable pageable) {
        Page<ActivityLog> logsPage = activityLogRepo.findByTargetEntityIdAndTargetEntityTypeOrderByTimestampDesc(targetEntityId, targetEntityType, pageable);
        return logsPage.map(log -> {
            ActivityLogDTO dto = activityLogMapper.toDTO(log);
            if (log.getActor() != null) {
                dto.setActorUsername(log.getActor().getUsername());
            }
            dto.setTargetEntityDescription(getTargetEntityDescription(log.getTargetEntityId(), log.getTargetEntityType()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable) {
        Page<ActivityLog> logsPage = activityLogRepo.findAll(pageable);
        return logsPage.map(log -> {
            ActivityLogDTO dto = activityLogMapper.toDTO(log);
            if (log.getActor() != null) {
                dto.setActorUsername(log.getActor().getUsername());
            }
            dto.setTargetEntityDescription(getTargetEntityDescription(log.getTargetEntityId(), log.getTargetEntityType()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyActivityStatsDTO getMonthlyDashboardStats(YearMonth yearMonth) {
        String monthStr = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        MonthlyActivityStatsProjection projection = activityLogRepo.getAggregatedMonthlyStatsNative(monthStr);

        if (projection == null) {
            return new MonthlyActivityStatsDTO(monthStr); // Return DTO with zeros
        }

        // Manually map from Projection to DTO
        MonthlyActivityStatsDTO dto = new MonthlyActivityStatsDTO(projection.getMonth());
        dto.setChantiersCreated(projection.getChantiersCreated() != null ? projection.getChantiersCreated() : 0);
        dto.setChantiersCompleted(projection.getChantiersCompleted() != null ? projection.getChantiersCompleted() : 0);
        dto.setChantiersWithPdpPending(projection.getChantiersWithPdpPending() != null ? projection.getChantiersWithPdpPending() : 0);
        dto.setChantiersWithBdtPending(projection.getChantiersWithBdtPending() != null ? projection.getChantiersWithBdtPending() : 0);
        dto.setDocumentsSigned(projection.getDocumentsSigned() != null ? projection.getDocumentsSigned() : 0);
        dto.setActionsRequiredOnDocuments(projection.getActionsRequiredOnDocuments() != null ? projection.getActionsRequiredOnDocuments() : 0);

        // Fetch additional stats
        Date monthStartDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date monthEndDate = Date.from(yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toInstant());
        Long activeChantiersCount = chantierRepo.countChantiersActiveDuringMonth(
                monthStartDate,
                monthEndDate,
                List.of(ChantierStatus.COMPLETED, ChantierStatus.CANCELED)
        );
        dto.setChantiersActiveDuringMonth(activeChantiersCount != null ? activeChantiersCount.intValue() : 0);

        Long documentsPendingCount = documentRepo.countDocumentsBecameStatusInMonth(
                DocumentStatus.NEEDS_ACTION,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
        dto.setDocumentsCurrentlyNeedingAction(documentsPendingCount != null ? documentsPendingCount.intValue() : 0);

        log.info("Fetched monthly dashboard stats for {}: {}", monthStr, dto);
        return dto;
    }
}