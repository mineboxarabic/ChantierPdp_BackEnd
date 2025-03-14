package com.danone.pdpbackend.Utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PdfData {
    @JsonProperty("pdfData")
    @Column(name = "pdf_data")
    private byte[] pdfData;
}
