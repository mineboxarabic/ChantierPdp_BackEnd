package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.ActivityLogService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.ActivityLogDTO;
import com.danone.pdpbackend.entities.dto.MonthlyActivityStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/dashboard") // Or "/api/reporting"
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ActivityLogService activityLogService;

    @GetMapping("/monthly-stats")
    public ResponseEntity<ApiResponse<MonthlyActivityStatsDTO>> getMonthlyStats(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        try {

            if (month == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Le paramètre 'month' (YYYY-MM) est requis. ('month' parameter (YYYY-MM) is required.)"));
            }
            MonthlyActivityStatsDTO stats = activityLogService.getMonthlyDashboardStats(month);
            return ResponseEntity.ok(new ApiResponse<>(stats, "Statistiques mensuelles récupérées. (Monthly statistics retrieved.)"));
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format for month parameter: {}", month, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Format de date invalide pour 'month'. Utilisez YYYY-MM. (Invalid date format for 'month'. Use YYYY-MM.)"));
        } catch (Exception e) {
            log.error("Error fetching monthly stats for {}", month, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }

    @GetMapping("/activity-log/target/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<Page<ActivityLogDTO>>> getActivityLogForTarget(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<ActivityLogDTO> logs = activityLogService.getActivityLogsForTarget(entityId, entityType, pageable);
            return ResponseEntity.ok(new ApiResponse<>(logs, "Journal d'activité récupéré pour l'entité. (Activity log retrieved for entity.)"));
        } catch (Exception e) {
            log.error("Error fetching activity log for {} ID {}", entityType, entityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }

    // This endpoint might be admin-only
    @GetMapping("/activity-log/all")
    public ResponseEntity<ApiResponse<Page<ActivityLogDTO>>> getAllActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<ActivityLogDTO> logs = activityLogService.getAllActivityLogs(pageable);
            return ResponseEntity.ok(new ApiResponse<>(logs, "Tous les journaux d'activité récupérés. (All activity logs retrieved.)"));
        } catch (Exception e) {
            log.error("Error fetching all activity logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }


}