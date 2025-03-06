package com.danone.pdpbackend.Utils;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class MisesEnDisposition {

    Boolean vestiaires = false;
    Boolean sanitaires = false;
    Boolean restaurant = false;
    Boolean energie = false;

}
