package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Notification;
import com.danone.pdpbackend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    // Find by target user, optionally filter by read status, and paginate
    Page<Notification> findByTargetUserAndIsRead(User targetUser, boolean isRead, Pageable pageable);
    Page<Notification> findByTargetUser(User targetUser, Pageable pageable);

    // Count unread notifications for a user
    long countByTargetUserAndIsReadFalse(User targetUser);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetUser = :targetUser AND n.isRead = false")
    int markAllAsReadForUser(@Param("targetUser") User targetUser);
}