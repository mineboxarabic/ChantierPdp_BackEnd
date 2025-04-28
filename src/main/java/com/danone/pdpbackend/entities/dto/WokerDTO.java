package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
public class WokerDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Long entreprise;
    private List<Long> pdps;
    private List<Long> signatures;
    private List<Long> chantiers;
    private List<Long> chantierSelections;
}
