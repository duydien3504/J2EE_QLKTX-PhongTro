package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN p.invoice i " +
            "LEFT JOIN i.contract c " +
            "LEFT JOIN c.room r " +
            "LEFT JOIN r.floor f " +
            "LEFT JOIN f.building b " +
            "LEFT JOIN b.manager m " +
            "LEFT JOIN b.owner o " +
            "WHERE :isAdmin = true " +
            "OR m.username = :username " +
            "OR o.username = :username")
    Page<Payment> findAllCustom(@Param("username") String username,
                                @Param("isAdmin") boolean isAdmin,
                                Pageable pageable);
}
