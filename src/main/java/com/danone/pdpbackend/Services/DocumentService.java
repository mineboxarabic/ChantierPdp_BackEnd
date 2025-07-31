package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.dto.DocumentDTO;
import jakarta.transaction.Transactional;

public interface DocumentService extends CommonDocumentServiceInterface<Document>{

    @Transactional
    Document addSignature(Long documentId, DocumentSignature documentSignature);

    Document calculateDocumentState(Long documentId);

    Document calculateDocumentState(Document document);

    Document updateDocumentStatus(Document document);

    @Transactional
    DocumentDTO duplicateDocument(Long documentId);
}
