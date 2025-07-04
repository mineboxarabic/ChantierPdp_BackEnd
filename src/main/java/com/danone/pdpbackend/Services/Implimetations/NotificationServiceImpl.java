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

    // --- Implementation of Helper Methods ---

    private String getDocumentTypeName(Document document) {
        if (document instanceof Pdp) return "PDP";
        if (document instanceof Bdt) return "BDT";
        return "Document";
    }

    private String getDocumentLink(Document document) {
        String typePath = (document instanceof Pdp) ? "pdp" : "bdt";
        return String.format("/documents/%s/%d", typePath, document.getId());
    }

    private String getChantierDescription(Chantier chantier){
        return String.format("Chantier '%s' (ID: %d)", chantier.getNom(), chantier.getId());
    }

    private String getDocumentDescription(Document document) {
        String docType = getDocumentTypeName(document);
        String chantierName = (document.getChantier() != null) ? document.getChantier().getNom() : "N/A";
        return String.format("%s ID: %d (Chantier: %s)", docType, document.getId(), chantierName);
    }


    @Override
    public void notifyChantierStatusProblem(Chantier chantier, String problemDetails) {
        if (chantier == null || chantier.getDonneurDOrdre() == null) return;
        String message = String.format("Problème avec le chantier '%s': %s. Statut actuel: %s.",
                chantier.getNom(), problemDetails, chantier.getStatus());
        createNotification(chantier.getDonneurDOrdre(), NotificationType.CHANTIER_STATUS_BAD, message,
                chantier.getId(), "Chantier", "/chantiers/" + chantier.getId(), getChantierDescription(chantier));
        // TODO: Notify other relevant parties (e.g., EE/EU managers)
    }

    @Override
    public void notifyChantierRequiresPdp(Chantier chantier) {
        if (chantier == null || chantier.getDonneurDOrdre() == null) return;
        String message = String.format("Le chantier '%s' requiert un PDP.", chantier.getNom());
        createNotification(chantier.getDonneurDOrdre(), NotificationType.CHANTIER_PENDING_PDP, message,
                chantier.getId(), "Chantier", "/chantiers/" + chantier.getId() + "/pdp/new", getChantierDescription(chantier));
    }

    @Override
    public void notifyChantierRequiresBdt(Chantier chantier) {
        if (chantier == null || chantier.getDonneurDOrdre() == null) return;
        String message = String.format("Le chantier '%s' requiert un BDT pour aujourd'hui.", chantier.getNom());
        createNotification(chantier.getDonneurDOrdre(), NotificationType.CHANTIER_PENDING_BDT, message,
                chantier.getId(), "Chantier", "/chantiers/" + chantier.getId() + "/bdt/today", getChantierDescription(chantier));
    }

    @Override
    public void notifyChantierInactiveToday(Chantier chantier) {
        if (chantier == null || chantier.getDonneurDOrdre() == null) return;
        String message = String.format("Le chantier '%s' est inactif aujourd'hui (BDT manquant ou non signé).", chantier.getNom());
        createNotification(chantier.getDonneurDOrdre(), NotificationType.CHANTIER_INACTIVE_TODAY, message,
                chantier.getId(), "Chantier", "/chantiers/" + chantier.getId() + "/bdt/today", getChantierDescription(chantier));
    }


    @Override
    public void notifyDocumentCompleted(Document document) {
        if (document == null || document.getChantier() == null || document.getChantier().getDonneurDOrdre() == null) return;
        String docType = getDocumentTypeName(document);
        String message = String.format("Le %s pour le chantier '%s' est complété.", docType, document.getChantier().getNom());
        createNotification(document.getChantier().getDonneurDOrdre(), NotificationType.DOCUMENT_COMPLETED, message,
                document.getId(), docType, getDocumentLink(document), getDocumentDescription(document));
    }

    @Override
    public void notifyDocumentActionNeeded(Document document, String specificActionMessage, NotificationType type) {
        if (document == null || document.getChantier() == null || document.getChantier().getDonneurDOrdre() == null) return;
        String docType = getDocumentTypeName(document);
        String message = String.format("Action requise pour le %s du chantier '%s': %s.",
                docType, document.getChantier().getNom(), specificActionMessage);
        createNotification(document.getChantier().getDonneurDOrdre(), type, message,
                document.getId(), docType, getDocumentLink(document), getDocumentDescription(document));
        // Potentially notify other users based on specificActionMessage or document state.
    }

    @Override
    public void notifyDocumentSignatureMissing(Document document, List<User> usersToSign) {
        if (document == null || usersToSign == null || usersToSign.isEmpty()) return;
        String docType = getDocumentTypeName(document);
        String chantierName = document.getChantier() != null ? document.getChantier().getNom() : "N/A";
        String message = String.format("Votre signature est requise pour le %s du chantier '%s'.", docType, chantierName);

        for (User user : usersToSign) {
            createNotification(user, NotificationType.DOCUMENT_SIGNATURE_MISSING, message,
                    document.getId(), docType, getDocumentLink(document), getDocumentDescription(document));
        }
    }

    @Override
    public void notifyDocumentPermitMissing(Document document) {
        if (document == null || document.getChantier() == null || document.getChantier().getDonneurDOrdre() == null) return;
        // Logic to determine who is responsible for permits (e.g., DonneurDOrdre or EE manager)
        User responsibleUser = document.getChantier().getDonneurDOrdre(); // Example
        String docType = getDocumentTypeName(document);
        String message = String.format("Un permis est manquant pour le %s du chantier '%s'.", docType, document.getChantier().getNom());

        createNotification(responsibleUser, NotificationType.DOCUMENT_PERMIT_MISSING, message,
                document.getId(), docType, getDocumentLink(document) + "/permits", getDocumentDescription(document));
    }

    @Override
    public void notifyDocumentExpired(Document document) {
        if (document == null || document.getChantier() == null || document.getChantier().getDonneurDOrdre() == null) return;
        String docType = getDocumentTypeName(document);
        String message = String.format("Le %s pour le chantier '%s' a expiré et nécessite une révision/renouvellement.", docType, document.getChantier().getNom());
        createNotification(document.getChantier().getDonneurDOrdre(), NotificationType.DOCUMENT_EXPIRED, message,
                document.getId(), docType, getDocumentLink(document), getDocumentDescription(document));
    }

}