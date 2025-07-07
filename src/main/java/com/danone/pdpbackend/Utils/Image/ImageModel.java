package com.danone.pdpbackend.Utils.Image;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;


@Embeddable
@Getter
@Setter
public class ImageModel {
    @JsonProperty("mimeType")
    private String mimeType;
    @JsonProperty("imageData")
    @Column(name = "image_data")
    private byte[] imageData;

    public ImageModel(byte[] signatureImageBytes) {
        this.imageData = signatureImageBytes;
    }

    public ImageModel() {

    }
}
