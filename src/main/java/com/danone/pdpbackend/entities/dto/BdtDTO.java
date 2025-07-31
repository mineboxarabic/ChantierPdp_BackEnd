package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.BDT.ComplementOuRappel;
import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BdtDTO extends DocumentDTO {


    private Long id;

    private String nom;

    private List<ComplementOuRappel> complementOuRappels;

    private LocalDate date;

    private Boolean personnelDansZone;

    private String horaireDeTravaille;

    private String tachesAuthoriser;
}