package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.PdfData;
import com.danone.pdpbackend.Utils.PermiTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "permit")
@Getter
@Setter
public class Permit extends InfoDeBase{

    @Enumerated(EnumType.STRING)  // Changed from EnumType.STRING to EnumType.ORDINAL
    private PermiTypes type;

    private byte[] pdfData;
}
