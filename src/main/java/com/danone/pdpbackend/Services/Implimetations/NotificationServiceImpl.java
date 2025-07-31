package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.NotificationRepo;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Services.NotificationService;
import com.danone.pdpbackend.Utils.NotificationType;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.NotificationDTO;
import com.danone.pdpbackend.Utils.mappers.NotificationMapper; // You'll create this mapper
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final UsersRepo usersRepo; // Assuming you'll get User entity if only username is passed
    private final NotificationMapper notificationMapper; // Inject your mapper



    public Boolean isNotificationExists(User targetUser, NotificationType type, String message, Long relatedEntityId, String relatedEntityType) {
        return notificationRepo.findByTargetUserAndTypeAndMessageAndRelatedEntityIdAndRelatedEntityType(targetUser, type, message, relatedEntityId, relatedEntityType);
    } 


    @Override
    @Transactional
    public NotificationDTO createNotification(User targetUser, NotificationType type, String message, Long relatedEntityId, String relatedEntityType, String callToActionLink, String relatedEntityDescription) {
        if (targetUser == null) {
            log.error("Target user cannot be null for notification: type={}, message={}", type, message);
            // Depending on requirements, you could throw an exception or log and return null
            return null; // Or throw new IllegalArgumentException("Target user is required for notification");
        }

        Notification notification = Notification.builder()
                .targetUser(targetUser)
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .relatedEntityDescription(relatedEntityDescription) // You'll need to set this
                .callToActionLink(callToActionLink)
                .isRead(false)
                .build(); // Timestamp is set by @PrePersist
        
            Boolean isNotificationExists = notificationRepo.findByTargetUserAndTypeAndMessageAndRelatedEntityIdAndRelatedEntityType(targetUser, type, message, relatedEntityId, relatedEntityType);
   
        if (isNotificationExists != null && isNotificationExists) {
            log.warn("Notification already exists for user {}: type={}, message={}", targetUser.getUsername(), type, message);
            return null; // Or throw an exception if you want to enforce uniqueness
        }


                Notification savedNotification = notificationRepo.save(notification);
            log.info("Notification created for user {}: {}", targetUser.getUsername(), message);
        return notificationMapper.toDTO(savedNotification);
    }


    public User getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return usersRepo.findByUsername(username).orElse(null);
        }
        return null;
    }


    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationType type, String message, Long relatedEntityId, String relatedEntityType, String callToActionLink, String relatedEntityDescription) {

        User currnetUser =  getCurrentActor();

        if (currnetUser == null) {
            log.error("Target user cannot be null for notification: type={}, message={}", type, message);
            // Depending on requirements, you could throw an exception or log and return null
            return null; // Or throw new IllegalArgumentException("Target user is required for notification");
        }

        Notification notification = Notification.builder()
                .targetUser(currnetUser)
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .relatedEntityDescription(relatedEntityDescription) // You'll need to set this
                .callToActionLink(callToActionLink)
                .isRead(false)
                .build(); // Timestamp is set by @PrePersist
        Notification savedNotification = notificationRepo.save(notification);
        log.info("Notification created for user {}: {}", currnetUser.getUsername(), message);
        return notificationMapper.toDTO(savedNotification);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsForUser(User targetUser, String readStatus, Pageable pageable) {
        Page<Notification> notificationsPage;
        if ("false".equalsIgnoreCase(readStatus)) {
            notificationsPage = notificationRepo.findByTargetUserAndIsRead(targetUser, false, pageable);
        } else if ("true".equalsIgnoreCase(readStatus)) {
            notificationsPage = notificationRepo.findByTargetUserAndIsRead(targetUser, true, pageable);
        } else { // "all" or any other value
            notificationsPage = notificationRepo.findByTargetUser(targetUser, pageable);
        }
        return notificationsPage.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId + " Ya KELB (يا كلب - you dog)"));

        // Security check: ensure the currentUser is the targetUser of the notification
        if (!notification.getTargetUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to mark notification {} as read, but it belongs to user {}",
                    currentUser.getUsername(), notificationId, notification.getTargetUser().getUsername());
            throw new SecurityException("You can only mark your own notifications as read, espèce de con (you idiot).");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepo.save(notification);
        log.info("Notification {} marked as read for user {}", notificationId, currentUser.getUsername());
        return notificationMapper.toDTO(updatedNotification);
    }

    @Override
    @Transactional
    public int markAllAsRead(User currentUser) {
        int updatedCount = notificationRepo.markAllAsReadForUser(currentUser);
        log.info("{} notifications marked as read for user {}", updatedCount, currentUser.getUsername());
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User targetUser) {
        return notificationRepo.countByTargetUserAndIsReadFalse(targetUser);
    }

}