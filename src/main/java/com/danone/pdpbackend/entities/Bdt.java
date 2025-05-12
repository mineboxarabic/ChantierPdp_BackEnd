package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.BDT.ComplementOuRappel;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "BDT")
@Getter
@Setter
public class Bdt extends Document {


    private String nom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ComplementOuRappel> complementOuRappels;

    private LocalDate date;
}