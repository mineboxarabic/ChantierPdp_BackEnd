package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.AuditSecu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditSecuRepo extends JpaRepository<AuditSecu, Long> {
}
