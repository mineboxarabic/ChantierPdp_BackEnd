package com.danone.pdpbackend.entities.dto;

import lombok.Data;

import java.util.Date;
@Data
public class WorkerChantierSelectionDTO {
    private Long id;

    private Long worker;

    private Long chantier;

    private Date selectionDate;

    private Boolean isSelected = true;

    private String selectionNote;


    private Long selectedBy;
}
