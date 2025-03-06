package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.BDT.BDT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BDTRepo extends JpaRepository<BDT, Long> {
}