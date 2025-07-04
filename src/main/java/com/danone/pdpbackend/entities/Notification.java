package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.NotificationType; // You'll create this enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column(nullable = false, length = 500)
    private String message; // What went wrong or what's up

    @Column(nullable = false)
    private LocalDateTime timestamp; // When this happened

    @Column(nullable = false)
    private boolean isRead = false; // Did they see it yet?

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // So you know what kind of clusterfuck it is

    private Long relatedEntityId; // ID of the Chantier, Pdp, Bdt that's causing trouble
    private String relatedEntityType; // "Chantier", "Pdp", "Bdt"

    @Column(length = 255)
    private String callToActionLink; // A handy link to jump straight to the problem

    private String relatedEntityDescription;
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}