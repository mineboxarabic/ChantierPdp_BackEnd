package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Signature;
import jakarta.transaction.Transactional;

public interface DocumentService<DOCUMENT_TYPE> extends Service<DOCUMENT_TYPE>{

    @Transactional
    Document addSignature(Long documentId, DocumentSignature documentSignature);

}
