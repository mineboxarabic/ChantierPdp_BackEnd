package com.danone.pdpbackend.entities.dto;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalisationDTO {

    private Long id;

    private String nom;
    private String code;
    private String description;

}
