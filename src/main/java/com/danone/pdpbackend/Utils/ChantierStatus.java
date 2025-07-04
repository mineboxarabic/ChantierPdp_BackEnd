package com.danone.pdpbackend.Utils;

public enum ChantierStatus {
    PENDING_PDP, // If no Active PDP is linked to the chantier
    PENDING_BDT, //If PDP is not required and BDT is not present
    ACTIVE, //No PDP required Or PDP is active and BDT present and signed (Completed)
    COMPLETED, //This completed is for the chantier (So that we know the chantier of this  is completed)
    CANCELED, //If the chantier is canceled
    INACTIVE_TODAY, //BDT is not present or not signed
}
