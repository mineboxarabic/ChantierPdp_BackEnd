package com.danone.pdpbackend.Repo;


import com.danone.pdpbackend.entities.Signature;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureRepo extends org.springframework.data.repository.Repository<Signature, Long> {


    Signature save(Signature signature);

    Signature findById(Long signatureChargeDeTravail);
    List<Signature> findSignaturesByIdIn(List<Long> ids);
}
