package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.UserService;
import com.danone.pdpbackend.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.AppUser;
import com.danone.pdpbackend.entities.Entreprise;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EntrepriseServiceImplTest {

    @Autowired
    private EntrepriseService entrepriseService;

    @Autowired
    private UserService userService;

    @Test
    void create() {
/*        // Arrange
        AppUser appUser = userService.getUserById(101L);
        assertNotNull(appUser, "AppUser should not be null. Make sure the user exists in the database or mock it.");

        EntrepriseDTO entrepriseDto = EntrepriseDTO.builder().build();
        entrepriseDto.setNom("Danone");
        entrepriseDto.setFonction("Agroalimentaire");
        entrepriseDto.setNumTel("123456789");
        entrepriseDto.setReferentPdp(appUser);
        entrepriseDto.setResponsableChantier(appUser);
        entrepriseDto.setRaisonSociale("Danone");

        // Act
        entrepriseService.create(entrepriseDto);

        // Assert
        Entreprise entreprise = entrepriseService.findEntrepriseById(entrepriseService.findMaxId());
        assertNotNull(entreprise, "Entreprise should not be null after creation.");
        assertEquals("Danone", entreprise.getNom(), "Entreprise name should match the provided name.");*/
    }

}
