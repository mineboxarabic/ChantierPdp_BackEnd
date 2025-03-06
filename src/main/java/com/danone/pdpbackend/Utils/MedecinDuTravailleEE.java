package com.danone.pdpbackend.Utils;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class MedecinDuTravailleEE {

   String nom;
   String noTel;

}
