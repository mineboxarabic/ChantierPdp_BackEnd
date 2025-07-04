package com.danone.pdpbackend.entities.dto;

public interface MonthlyActivityStatsProjection {
    String getMonth();
    Integer getChantiersCreated();
    Integer getChantiersCompleted();
    Integer getChantiersWithPdpPending();
    Integer getChantiersWithBdtPending();
    Integer getDocumentsSigned();
    Integer getActionsRequiredOnDocuments();
}
