package com.danone.pdpbackend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyActivityStatsDTO {
    private String month;
    private int chantiersCreated;
    private int chantiersCompleted;
    private int chantiersWithPdpPending;
    private int chantiersWithBdtPending;
    private int documentsSigned;
    private int actionsRequiredOnDocuments; // This was 0L in your query, might need to be long or int
    // New Fields
    private int chantiersActiveDuringMonth;     // Chantiers whose active period overlapped with the month
    private int documentsCurrentlyNeedingAction; // Snapshot: Documents currently in NEEDS_ACTION status
    // Constructor for JPA Projection - ensure types match query output (SUM returns Long)
    public MonthlyActivityStatsDTO(String month,
                                   Long chantiersCreated,
                                   Long chantiersCompleted,
                                   Long chantiersWithPdpPending,
                                   Long chantiersWithBdtPending,
                                   Long documentsSigned,
                                   Long actionsRequiredOnDocuments,

                                   Long chantiersActiveDuringMonth,
                                   Long documentsCurrentlyNeedingAction) {
        this.month = month;
        // Be careful with potential NullPointerExceptions if SUM can return null (e.g., no matching rows)
        // Coalesce to 0L if necessary in the query: SUM(COALESCE(CASE..., 0))
        this.chantiersCreated = (chantiersCreated != null) ? chantiersCreated.intValue() : 0;
        this.chantiersCompleted = (chantiersCompleted != null) ? chantiersCompleted.intValue() : 0;
        this.chantiersWithPdpPending = (chantiersWithPdpPending != null) ? chantiersWithPdpPending.intValue() : 0;
        this.chantiersWithBdtPending = (chantiersWithBdtPending != null) ? chantiersWithBdtPending.intValue() : 0;
        this.documentsSigned = (documentsSigned != null) ? documentsSigned.intValue() : 0;
        this.actionsRequiredOnDocuments = (actionsRequiredOnDocuments != null) ? actionsRequiredOnDocuments.intValue() : 0;
        this.chantiersActiveDuringMonth = (chantiersActiveDuringMonth != null) ? chantiersActiveDuringMonth.intValue() : 0;
        this.documentsCurrentlyNeedingAction = (documentsCurrentlyNeedingAction != null) ? documentsCurrentlyNeedingAction.intValue() : 0;

    }
    // For creating empty DTO in service if query returns null
    public MonthlyActivityStatsDTO(String month) {
        this.month = month;
        this.chantiersCreated = 0;
        this.chantiersCompleted = 0;
        this.chantiersWithPdpPending = 0;
        this.chantiersWithBdtPending = 0;
        this.documentsSigned = 0;
        this.actionsRequiredOnDocuments = 0;
        this.chantiersActiveDuringMonth = 0;
        this.documentsCurrentlyNeedingAction = 0;
    }

    // Add more metrics as you see fit
}