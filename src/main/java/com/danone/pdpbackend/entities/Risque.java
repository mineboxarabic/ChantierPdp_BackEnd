package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity(name = "risque")
@Getter
@Setter
public class Risque extends InfoDeBase{
    private Boolean travailleDangereux;
    private Boolean travaillePermit;

    public Long permitId;
}
