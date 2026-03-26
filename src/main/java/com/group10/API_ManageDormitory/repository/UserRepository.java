package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false " +
            "AND (:roleName IS NULL OR LOWER(u.role.roleName) = LOWER(:roleName)) " +
            "AND (:isOwner = false OR (u.role.roleName NOT IN ('ADMIN', 'OWNER', 'SCOPE_ADMIN', 'SCOPE_OWNER'))) " +
            "AND (:searchTerm IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findAllFiltered(@Param("roleName") String roleName,
                               @Param("isOwner") boolean isOwner,
                               @Param("searchTerm") String searchTerm,
                               Pageable pageable);
}
