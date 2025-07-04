package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.NotificationService;
import com.danone.pdpbackend.Services.UserService; // Assuming you have this to fetch User by username
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService; // To fetch the current User entity

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Putain de merde! User not authenticated. (User not authenticated!)");
        }
        String username = authentication.getName();
        return userService.findByUsername(username); // Or findByUsername, depending on your User entity and UserService
        // Adjust if your User entity doesn't have findByEmail or if username is not email.
        // Make sure this method in UserService returns a User or throws UsernameNotFoundException.
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUserNotifications(
            @RequestParam(defaultValue = "false") String readStatus, // "false", "true", or "all"
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<NotificationDTO> notifications = notificationService.getNotificationsForUser(currentUser, readStatus, pageable);
            return ResponseEntity.ok(new ApiResponse<>(notifications, "Notifications récupérées. (Notifications retrieved.)"));
        } catch (UsernameNotFoundException e) {
            log.error("Error fetching notifications: User not found.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(null, "Utilisateur non trouvé. (User not found.)"));
        } catch (Exception e) {
            log.error("Error fetching notifications for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markNotificationAsRead(@PathVariable Long id) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            NotificationDTO updatedNotification = notificationService.markAsRead(id, currentUser);
            return ResponseEntity.ok(new ApiResponse<>(updatedNotification, "Notification marquée comme lue. (Notification marked as read.)"));
        } catch (UsernameNotFoundException e) {
            log.error("Error marking notification as read: User not found.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(null, "Utilisateur non trouvé. (User not found.)"));
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("Notification not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking notification as read for id {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<String>> markAllNotificationsAsRead() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            int count = notificationService.markAllAsRead(currentUser);
            return ResponseEntity.ok(new ApiResponse<>(count + " notifications marquées comme lues.", "Toutes les notifications marquées comme lues. (All notifications marked as read.)"));
        } catch (UsernameNotFoundException e) {
            log.error("Error marking all notifications as read: User not found.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(null, "Utilisateur non trouvé. (User not found.)"));
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            long count = notificationService.getUnreadNotificationCount(currentUser);
            return ResponseEntity.ok(new ApiResponse<>(count, "Nombre de notifications non lues. (Unread notification count.)"));
        } catch (UsernameNotFoundException e) {
            log.error("Error fetching unread count: User not found.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(null, "Utilisateur non trouvé. (User not found.)"));
        } catch (Exception e) {
            log.error("Error fetching unread notification count for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, "Erreur serveur interne. (Internal server error.)"));
        }
    }
}