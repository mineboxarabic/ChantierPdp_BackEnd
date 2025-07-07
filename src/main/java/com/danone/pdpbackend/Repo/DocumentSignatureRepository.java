package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Long> {
}
