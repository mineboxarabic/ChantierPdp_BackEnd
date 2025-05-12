package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
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
    private Long document;

    private ObjectAnsweredObjects objectType; // "risque", "dispositif", "permit", ...
    private Long objectId;
    //Answers
    Boolean answer;
    Boolean EE;
    Boolean EU;
}
