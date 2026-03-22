package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.UserNotification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Integer> {
    @EntityGraph(attributePaths = {"notification"})
    List<UserNotification> findByUser_UserIdOrderByNotification_CreatedDateDesc(Integer userId);
}
