package com.danone.pdpbackend.entities.BDT;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ComplementOuRappel implements Serializable {
    private String complement;
    private Boolean respect;
}
