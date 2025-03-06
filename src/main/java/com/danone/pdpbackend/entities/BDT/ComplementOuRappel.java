package com.danone.pdpbackend.entities.BDT;


import jakarta.persistence.*;

@Entity(name = "ComplementOuRappel")
public class ComplementOuRappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private BDT bdt;

    private String complement;
    private Boolean respect;
}
