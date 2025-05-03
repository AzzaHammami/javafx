package com.example.rendez_vous.repositories;

import com.example.rendez_vous.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(Integer userId);
}
