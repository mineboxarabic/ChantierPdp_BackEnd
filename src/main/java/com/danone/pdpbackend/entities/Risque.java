package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;


@Entity(name = "risque")
@Getter
@Setter
public class Risque extends InfoDeBase{
    private Boolean travailleDangereux;
    private Boolean travaillePermit;
}
