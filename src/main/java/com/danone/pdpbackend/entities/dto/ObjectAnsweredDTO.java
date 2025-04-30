package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.Bdt;
import com.danone.pdpbackend.entities.Pdp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectAnsweredDTO {
    private Long id;
    private Long pdp;
    private Long bdt;

    private ObjectAnsweredObjects objectType; // "risque", "dispositif", "permit", ...
    private Long objectId;
    //Answers
    Boolean answer;
    Boolean EE;
    Boolean EU;
}
