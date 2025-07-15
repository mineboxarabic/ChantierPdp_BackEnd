package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Long> {

    List<DocumentSignature> findByDocument(Document document);

    @Query("SELECT ds.worker FROM document_signature ds WHERE ds.document.id = :documentId")
    List<Worker> findWorkersByDocumentId(@Param("documentId") Long documentId);

    List<DocumentSignature> findDocumentSignatureByDocumentId(Long documentId);
    
}
