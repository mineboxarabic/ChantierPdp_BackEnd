package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.Notification;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.Utils.NotificationType;
import com.danone.pdpbackend.entities.dto.NotificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationService {

    NotificationDTO createNotification(User targetUser, NotificationType type, String message, Long relatedEntityId, String relatedEntityType, String callToActionLink, String relatedEntityDescription);

    @Transactional
    NotificationDTO createNotification(NotificationType type, String message, Long relatedEntityId, String relatedEntityType, String callToActionLink, String relatedEntityDescription);

    Page<NotificationDTO> getNotificationsForUser(User targetUser, String readStatus, Pageable pageable);

    NotificationDTO markAsRead(Long notificationId, User currentUser);

    int markAllAsRead(User currentUser);

    long getUnreadNotificationCount(User targetUser);

    // --- Helper methods to trigger notifications from other services ---
    void notifyChantierStatusProblem(Chantier chantier, String problemDetails);
    void notifyChantierRequiresPdp(Chantier chantier);
    void notifyChantierRequiresBdt(Chantier chantier);
    void notifyChantierInactiveToday(Chantier chantier);

    void notifyDocumentCompleted(Document document);
    void notifyDocumentActionNeeded(Document document, String specificActionMessage, NotificationType type); // More specific
    void notifyDocumentSignatureMissing(Document document, List<User> usersToSign);
    void notifyDocumentPermitMissing(Document document);
    void notifyDocumentExpired(Document document);

}