package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findAllByIsDeletedFalse();
    Optional<Contract> findByContractIdAndIsDeletedFalse(Integer id);
}
