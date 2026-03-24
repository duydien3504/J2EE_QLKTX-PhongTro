package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @Query("SELECT e.expenseType, SUM(e.amount) FROM Expense e GROUP BY e.expenseType")
    List<Object[]> getExpenseDistribution();
}

