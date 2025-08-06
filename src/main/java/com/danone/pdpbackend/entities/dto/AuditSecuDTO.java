package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.AuditType;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import lombok.Data;

@Data
public class AuditSecuDTO {
    private Long id;
    private String title;
    private String description;
    private ImageModel logo;
    private AuditType typeOfAudit; // Enum to distinguish audit types
}
