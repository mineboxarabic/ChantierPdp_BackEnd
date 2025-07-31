package com.danone.pdpbackend.Services.Implimetations;

import org.springframework.stereotype.Service;
import com.danone.pdpbackend.Repo.AuditSecuRepo;
import com.danone.pdpbackend.Services.AuditSecuService;
import com.danone.pdpbackend.entities.AuditSecu;

import java.util.List;
import java.util.Optional;

@Service
public class AuditSecuServiceImpl implements AuditSecuService {
    private final AuditSecuRepo auditSecuRepo;

    public AuditSecuServiceImpl(AuditSecuRepo auditSecuRepo) {
        this.auditSecuRepo = auditSecuRepo;
    }

    @Override
    public List<AuditSecu> getAllAuditSecus() {
        return auditSecuRepo.findAll();
    }

    @Override
    public AuditSecu getAuditSecuById(Long id) {
        return auditSecuRepo.findAuditSecuById(id);
    }

    @Override
    public List<AuditSecu> getAuditSecusByType(String typeOfAudit) {
        return auditSecuRepo.findByTypeOfAudit(typeOfAudit);
    }



    @Override
    public AuditSecu createAuditSecu(AuditSecu auditSecu) {
        return auditSecuRepo.save(auditSecu);
    }

    @Override
    public AuditSecu updateAuditSecu(Long id, AuditSecu auditSecuDetails) {
        Optional<AuditSecu> optionalAuditSecu = Optional.ofNullable(auditSecuRepo.findAuditSecuById(id));

        if (optionalAuditSecu.isEmpty()) {
            return null;
        }

        AuditSecu existingAuditSecu = optionalAuditSecu.get();

        // Update fields manually to avoid reflection issues
        if (auditSecuDetails.getTitle() != null) {
            existingAuditSecu.setTitle(auditSecuDetails.getTitle());
        }
        if (auditSecuDetails.getDescription() != null) {
            existingAuditSecu.setDescription(auditSecuDetails.getDescription());
        }
        if (auditSecuDetails.getLogo() != null) {
            existingAuditSecu.setLogo(auditSecuDetails.getLogo());
        }
        if (auditSecuDetails.getTypeOfAudit() != null) {
            existingAuditSecu.setTypeOfAudit(auditSecuDetails.getTypeOfAudit());
        }

        return auditSecuRepo.save(existingAuditSecu);
    }

    @Override
    public Boolean deleteAuditSecu(Long id) {
        Optional<AuditSecu> auditSecu = Optional.ofNullable(auditSecuRepo.findAuditSecuById(id));

        if (auditSecu.isEmpty()) {
            return false;
        } else {
            auditSecuRepo.deleteById(auditSecu.get().getId());
            return true;
        }
    }

}
