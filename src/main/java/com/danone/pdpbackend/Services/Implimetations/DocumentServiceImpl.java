package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Services.DocumentService;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService<Document> {
    private final DocumentRepo documentRepo;

    public DocumentServiceImpl(DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    @Override
    public List<Document> getAll() {
        return documentRepo.findAll();
    }

    @Override
    public Document getById(Long id) {
        return documentRepo.findById(id).orElse(null);
    }

    @Override
    public Document create(Document entity) {
        return documentRepo.save(entity);
    }

    @Override
    public Document update(Long id, Document entityDetails) {
        return documentRepo.save(entityDetails);
    }

    @Override
    public Boolean delete(Long id) {
        if(documentRepo.existsById(id)) {
            documentRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Document> getByIds(List<Long> ids) {
        return documentRepo.findDocumentsByIdIn(ids);
    }


    @Override
    @Transactional
    public Document addSignature(Long documentId, DocumentSignature documentSignature) {
        Document document = documentRepo.findById(documentId).orElse(null);
        if (document != null) {
            document.getSignatures().add(documentSignature);
            return documentRepo.save(document);
        }
        return null;
    }

    public Document addSignature(Document document, DocumentSignature documentSignature) {
        if (document != null) {
            document.getSignatures().add(documentSignature);
            return documentRepo.save(document);
        }
        return null;
    }
}
