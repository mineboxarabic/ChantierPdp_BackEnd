package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.Image.ImageModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DispositifDTO {
    private Long id;
    private String title;
    private String description;
    private ImageModel logo;
    private String type;
}
