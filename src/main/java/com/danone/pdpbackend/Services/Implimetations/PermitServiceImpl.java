package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.PermitRepo;
import com.danone.pdpbackend.Services.PermitService;
import com.danone.pdpbackend.entities.Permit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@Service
public class PermitServiceImpl implements PermitService {

    private final PermitRepo permitRepo;

    public PermitServiceImpl(PermitRepo permitRepo) {
        this.permitRepo = permitRepo;
    }

    @Override
    public List<Permit> getAllPermits() {
        return permitRepo.findAll();
    }

    @Override
    public Permit getPermitById(Long id) {
        return permitRepo.findPermitById(id);
    }

    @Override
    public Permit createPermit(Permit permit) {
        return permitRepo.save(permit);
    }

    @Override
    public Permit updatePermit(Long id, Permit permitDetails) {
        Permit permit = permitRepo.findPermitById(id);
        if (permit == null) {
            return null;
        }

        if (permitDetails.getTitle() != null) permit.setTitle(permitDetails.getTitle());
        if (permitDetails.getDescription() != null) permit.setDescription(permitDetails.getDescription());
        if (permitDetails.getLogo() != null) permit.setLogo(permitDetails.getLogo());
        if(permitDetails.getPdfData() != null) permit.setPdfData(permitDetails.getPdfData());


        return permitRepo.save(permit);
    }

    @Override
    public boolean deletePermit(Long id) {
        log.info("Deleting permit with id: " + id);
        permitRepo.deleteById(id);
        return true;
    }

    @Override
    public List<Permit> getPermitsByIds(List<Long> ids) {
        return permitRepo.findPermitByIdIn(ids);
    }


}
