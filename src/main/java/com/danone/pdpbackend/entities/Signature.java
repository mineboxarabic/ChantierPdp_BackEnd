package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity(name = "signature")
@Data
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    @ManyToOne
    private Document document;

    private Date date;


    @Embedded
    private ImageModel signature;

    @ManyToOne
    private Worker worker;
}
