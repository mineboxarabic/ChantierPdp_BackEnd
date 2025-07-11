package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Permit;
import java.util.List;

public interface PermitService {
    List<Permit> getAllPermits();

    Permit getPermitById(Long id);

    Permit createPermit(Permit permit);

    Permit updatePermit(Long id, Permit permitDetails);

    void delete(Long id);

    List<Permit> getPermitsByIds(List<Long> ids);
}
