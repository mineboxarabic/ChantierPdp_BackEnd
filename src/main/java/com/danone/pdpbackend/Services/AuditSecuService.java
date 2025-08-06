package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.AuditType;
import com.danone.pdpbackend.entities.AuditSecu;

import java.util.List;


public interface AuditSecuService {
    List<AuditSecu> getAllAuditSecus();

    List<AuditSecu> getAuditSecusByType(AuditType typeOfAudit);



    AuditSecu createAuditSecu(AuditSecu auditSecu);

    AuditSecu updateAuditSecu(Long id, AuditSecu auditSecuDetails);

    Boolean deleteAuditSecu(Long id);

    AuditSecu getAuditSecuById(Long id);
}