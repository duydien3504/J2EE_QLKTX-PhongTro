package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
}
