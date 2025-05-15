package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.ObjectAnswered;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
public class DocumentDTO {

    private Long id;

    private Long chantier;

    private Long entrepriseExterieure;

    private DocumentStatus status = DocumentStatus.DRAFT;
    private ActionType actionType = ActionType.NONE;

    private LocalDate date;

    private List<DocumentSignatureDTO> signatures = new ArrayList<>();

    private List<ObjectAnsweredDTO> relations = new ArrayList<>();

    private LocalDate creationDate = LocalDate.now(); // Track when it was created/became valid

}
