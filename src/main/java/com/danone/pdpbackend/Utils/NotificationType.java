package com.danone.pdpbackend.Utils;

public enum NotificationType {
    // Chantier related
    CHANTIER_STATUS_BAD,        // Chantier status is not good ( عام - *'aam* - generic)
    CHANTIER_PENDING_PDP,       // Waiting for PDP
    CHANTIER_PENDING_BDT,       // Waiting for BDT
    CHANTIER_INACTIVE_TODAY,    // Chantier should be active but isn't
    CHANTIER_COMPLETED_NOTICE,
    CHANTIER_CANCELED_NOTICE,

    // Document related
    DOCUMENT_COMPLETED,         // Document is done
    DOCUMENT_ACTION_NEEDED,     // Something needs to be done with a document
    DOCUMENT_SIGNATURE_MISSING, // Someone needs to sign this damn thing ( توقيع - *tawqie* - signature)
    DOCUMENT_PERMIT_MISSING,    // Missing a permit ( تصريح - *tasrih* - permit)
    DOCUMENT_EXPIRED,           // Document is old and dusty

    // General
    SYSTEM_ALERT
}