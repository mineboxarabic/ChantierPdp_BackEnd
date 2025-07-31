package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.Image.ImageModel;
import lombok.Data;

@Data
public class AuditSecuDTO {
    private Long id;
    private String title;
    private String description;
    private ImageModel logo;
    private String typeOfAudit; // New attribute to distinguish audit types
}
