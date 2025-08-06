package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.AuditType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AuditSecu")
@Getter
@Setter
public class AuditSecu extends InfoDeBase{
    @Enumerated(EnumType.STRING)
    private AuditType typeOfAudit; // Enum to distinguish audit types

}
