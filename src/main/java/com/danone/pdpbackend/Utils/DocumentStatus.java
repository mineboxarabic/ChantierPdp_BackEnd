package com.danone.pdpbackend.Utils;

public enum DocumentStatus {
    DRAFT, // If the document does not have a chantier.
    ACTIVE,         // Currently valid and fully signed (if required) and has chantier + Chantier is not canceled or completed + no permits are needed
    COMPLETED,      // The associated work/chantier is complited
    EXPIRED, // If it's been over a year since the creation //TODO: Work on expired
    CANCELED, // If the chantier is canceled
    NEEDS_ACTION,   // Requires signatures or other validation
    NEEDS_SIGNATURES, // Specifically needs worker signatures
    SIGNED          // Document has been fully signed by all required workers
}
