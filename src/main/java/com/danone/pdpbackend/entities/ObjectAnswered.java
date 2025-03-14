package com.danone.pdpbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "object_answered")
@Getter
@Setter
public class ObjectAnswered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    Risque risque;

    @ManyToOne
    Dispositif dispositif;

    @ManyToOne
    Permit permit;

    @ManyToOne
    AuditSecu auditSecu;

    Boolean answer;


}
