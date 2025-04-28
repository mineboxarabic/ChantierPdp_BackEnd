package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Services.EntrepriseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class EntrepriseTest {


    @Autowired
    private EntrepriseService entrepriseService;


/*
    @Test
    void createEntreprise(){
        // Arrange
        Entreprise entreprise = new Entreprise();
        entreprise.setNom("Danone");
        entreprise.setNumTel("123456789");
        entreprise.setRaisonSociale("Danone");

        // Act
        entrepriseService.create(entreprise);


    }

    @Test
    void changeAllImagesInDB() throws URISyntaxException, IOException {
        //Arange
        List<Entreprise> entreprises = entrepriseService.getAll();
        Path imagePath = Paths.get(getClass().getClassLoader().getResource("images.png").toURI());
        byte[] image = Files.readAllBytes(imagePath);

        //Act
        for (Entreprise entreprise : entreprises) {
            entreprise.setImage(image);
            entrepriseService.update(entreprise.getId(), entreprise);
         //   entrepriseService.createEntreprise(entreprise);
        }

        //Assert
        for (Entreprise entreprise : entreprises) {
            byte[] image1 = entrepriseService.getById(entreprise.getId()).getImage();
            assertArrayEquals(image, image1);
        }




    }
    @Test
    void changeAllImagesInDBToNull() throws URISyntaxException, IOException {
        //Arange
        List<Entreprise> entreprises = entrepriseService.getAll();
        Path imagePath = Paths.get(getClass().getClassLoader().getResource("images.png").toURI());
        byte[] image = Files.readAllBytes(imagePath);

        //Act
        for (Entreprise entreprise : entreprises) {
            entreprise.setImage(null);
            entrepriseService.update(entreprise.getId(), entreprise);
            //   entrepriseService.createEntreprise(entreprise);
        }

        //Assert
        for (Entreprise entreprise : entreprises) {
            byte[] image1 = entrepriseService.getById(entreprise.getId()).getImage();
            assertNull(image1);
        }




    }

    @Test
    void testGetNom() throws URISyntaxException, IOException {

        Path imagePath = Paths.get(getClass().getClassLoader().getResource("images.png").toURI());
        byte[] image = Files.readAllBytes(imagePath);

        log.info("path : {}", image);
        // Assert or use the image bytes as needed
             assertNotNull(image);


        //Log current path
       // log.info("path : {}", Paths.get("src/test/resources/images.png").toAbsolutePath());

      //  assert true;
    }*/


}