package com.danone.pdpbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "activity_log")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id") // Who did the thing? Can be null for system events.
    private User actor;

    @Column(nullable = false)
    private String actionKey; // e.g., "chantier.created", "document.status.changed"

    @Column(nullable = false)
    private LocalDateTime timestamp; // When it happened

    private Long targetEntityId;    // ID of the Chantier, Pdp, User etc.
    private String targetEntityType; // "Chantier", "Pdp", "Bdt", "User"

    @Column(length = 500)
    private String description; // A quick summary, e.g., "Chantier 'X' status changed to ACTIVE"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details; // Extra juicy bits, like old/new values

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}