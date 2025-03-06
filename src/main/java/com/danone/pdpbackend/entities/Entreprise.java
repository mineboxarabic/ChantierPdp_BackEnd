package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Entity(name = "entreprise")
@Getter
@Setter
public class Entreprise {

    @Id
    private Long id;

    private Boolean isUtilisatrice;

    private String nom;
    private String fonction;
    @Column(name = "numerotelephone")
    private String numTel;

    @JoinColumn(name = "referentpdp")
    @ManyToOne
    private AppUser referentPdp;

    @JoinColumn(name = "responsablechantier")
    @ManyToOne
    private AppUser responsableChantier;

    @Column(name = "raisonsociale")
    private String raisonSociale;

    @Column(name = "image")
    @Embedded
    private ImageModel image; // For storing binary data

    public Entreprise() {
       /* try {
            // Load the default image
            Path imagePath = Paths.get(getClass().getClassLoader().getResource("images.png").toURI());
            byte[] image = Files.readAllBytes(imagePath);
            this.image = image;
            log.info("path : {}", Paths.get("src/main/resources/image.png").toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
