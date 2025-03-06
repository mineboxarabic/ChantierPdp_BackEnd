package com.danone.pdpbackend.entities.BDT;


import com.danone.pdpbackend.entities.AuditSecu;
import com.danone.pdpbackend.entities.Risque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "BDT")
@Getter
@Setter
public class BDT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nom;


    @ManyToMany
    private List<Risque> risques;

    @ManyToMany
    private List<AuditSecu> auditSecu;


    @OneToMany(mappedBy = "bdt", cascade = CascadeType.ALL)
    private List<ComplementOuRappel> complementOuRappels;

}
