package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.AuditSecu;

import java.util.List;

public interface AuditSecuService {
    List<AuditSecu> getAllAuditSecus();

    AuditSecu getAuditSecuById(Long id);

    AuditSecu createAuditSecu(AuditSecu auditSecu);

    AuditSecu updateAuditSecu(Long id, AuditSecu auditSecuDetails);

    Boolean deleteAuditSecu(Long id);
}