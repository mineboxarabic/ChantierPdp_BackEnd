package com.danone.pdpbackend.Utils;

import jakarta.persistence.Embeddable;

@Embeddable
public enum PermiTypes {
    NONE,
    FOUILLE, //Permis de travail spécifique en fouille
    ATEX, //Permis d’intervention en zone ATEX
    ESPACE_CONFINE, //Permis de travail en espace confiné/restrient
    LEVAGE, //Permis de travail spécifique de levage
    HAUTEUR, //Permis de travail spécifique en hauteur
    TOITURE; //Permis de travail spécifique en toiture
}
