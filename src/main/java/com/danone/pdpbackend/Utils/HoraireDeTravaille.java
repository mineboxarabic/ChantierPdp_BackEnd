package com.danone.pdpbackend.Utils;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class HoraireDeTravaille {

    Boolean enJournee = false;
    Boolean enNuit = false;
    Boolean samedi = false;

}
