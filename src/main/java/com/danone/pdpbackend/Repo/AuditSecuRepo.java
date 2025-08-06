package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.Utils.AuditType;
import com.danone.pdpbackend.entities.AuditSecu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface AuditSecuRepo extends Repository<AuditSecu, Long> {

    AuditSecu save(AuditSecu auditSecu);

    Boolean delete(AuditSecu auditSecu);

    AuditSecu findAuditSecuById(Long id);

    List<AuditSecu> findAll();



    @Query("SELECT MAX(id) FROM AuditSecu")
    Long findMaxId();

    List<AuditSecu> findByTypeOfAudit(AuditType typeOfAudit);

    void deleteById(Long id);
}