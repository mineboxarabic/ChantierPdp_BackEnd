package com.danone.pdpbackend.entities.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ActivityLogDTO {
    private Long id;
    private String actorUsername; // Or UserDTO snippet
    private String actionKey;
    private String timestamp; // Send as ISO 8601 string
    private Long targetEntityId;
    private String targetEntityType;
    private String targetEntityDescription; // e.g. "Chantier: Alpha"
    private Map<String, Object> details;
}
