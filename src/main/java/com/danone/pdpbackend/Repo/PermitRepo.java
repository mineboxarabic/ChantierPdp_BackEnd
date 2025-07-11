package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Permit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;

@org.springframework.stereotype.Repository
public interface PermitRepo extends Repository<Permit, Long> {

    List<Permit> findAll();

    Permit findPermitById(Long id);

    Permit save(Permit permit);

    void deleteById(Long id);

    List<Permit> findPermitByIdIn(Collection<Long> ids);

    boolean existsById(Long id);
}
