package com.danone.pdpbackend.Services.Implimetations;

import org.springframework.stereotype.Service;
import com.danone.pdpbackend.Repo.AuditSecuRepo;
import com.danone.pdpbackend.Services.AuditSecuService;
import com.danone.pdpbackend.entities.AuditSecu;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
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
    public AuditSecu createAuditSecu(AuditSecu auditSecu) {
        return auditSecuRepo.save(auditSecu);
    }

    @Override
    public AuditSecu updateAuditSecu(Long id, AuditSecu auditSecuDetails) {
        Optional<AuditSecu> auditSecu = Optional.ofNullable(auditSecuRepo.findAuditSecuById(id));

        if (auditSecu.isEmpty()) {
            return null;
        }

        for(Field field : AuditSecu.class.getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object value = field.get(auditSecuDetails);
                if(value != null){
                    field.set(auditSecu.get(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return auditSecuRepo.save(auditSecuDetails);
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
