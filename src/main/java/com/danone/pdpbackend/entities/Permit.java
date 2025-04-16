package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.PdfData;
import com.danone.pdpbackend.Utils.PermiTypes;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "permit")
@Getter
@Setter
public class Permit extends InfoDeBase{
    private PermiTypes type;
    private byte[] pdfData;
}
