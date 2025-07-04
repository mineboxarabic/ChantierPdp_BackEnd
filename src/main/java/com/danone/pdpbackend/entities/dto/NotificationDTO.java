package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.NotificationType;
import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private String timestamp; // Send as ISO 8601 string
    private boolean isRead;
    private NotificationType type;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String relatedEntityDescription; // e.g., "Chantier: Alpha"
    private String callToActionLink;
}
