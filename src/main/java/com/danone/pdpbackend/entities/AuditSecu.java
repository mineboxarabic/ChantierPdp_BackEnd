package com.danone.pdpbackend.entities;


import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AuditSecu")
@Getter
@Setter
public class AuditSecu extends InfoDeBase{
    private String typeOfAudit; // New attribute to distinguish audit types

}
