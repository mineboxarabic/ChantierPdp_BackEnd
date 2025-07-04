package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.Image.ImageModel;
import lombok.Data;

@Data
public class SignatureRequestDTO {
    private Long workerId;
    private ImageModel signatureImage;
}
