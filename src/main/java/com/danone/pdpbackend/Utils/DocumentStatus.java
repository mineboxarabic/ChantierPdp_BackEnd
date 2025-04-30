package com.danone.pdpbackend.Utils;

public enum DocumentStatus {
    PLANNED,        // Newly created, not yet active or signed
    ACTIVE,         // Currently valid and fully signed (if required)
    NEEDS_ACTION,   // Requires signatures or other validation
    COMPLETED,      // The associated work/chantier is complited
    EXPIRED, // If it's been over a year since the creation //TODO: Work on expired
    NEEDS_SIGNATURES, //If not all signatures are present OR if there is no chantier assosiated//TODO: Work on signatures
    PERMIT_NEEDED, // If a permit was found in a risque but not in the relations
    CANCELED, // If the chantier is canceled
    DRAFT // If the document is in draft mode and not yet finalized
}
