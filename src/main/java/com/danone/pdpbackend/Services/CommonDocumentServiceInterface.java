package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import jakarta.transaction.Transactional;

public interface CommonDocumentServiceInterface<DOCUMENT_TYPE extends  Document> extends Service<DOCUMENT_TYPE>{

    @Transactional
    Document addSignature(Long documentId, DocumentSignature documentSignature);

    DOCUMENT_TYPE calculateDocumentState(Long documentId);

    DOCUMENT_TYPE calculateDocumentState(Document document);

    DOCUMENT_TYPE updateDocumentStatus(DOCUMENT_TYPE document);

}
