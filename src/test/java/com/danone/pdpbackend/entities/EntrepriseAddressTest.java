package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.EntrepriseType;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.Utils.mappers.EntrepriseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@SpringBootTest
public class EntrepriseAddressTest {

    @Test
    public void testEntrepriseEntityWithAddress() {
        // Test the entity
        Entreprise entreprise = Entreprise.builder()
                .nom("Test Company")
                .description("Test Description")
                .address("123 Test Street, Test City, Test Country")
                .type(EntrepriseType.EE)
                .build();
        
        assertNotNull(entreprise);
        assertEquals("Test Company", entreprise.getNom());
        assertEquals("123 Test Street, Test City, Test Country", entreprise.getAddress());
        assertEquals(EntrepriseType.EE, entreprise.getType());
    }

    @Test
    public void testEntrepriseDTOWithAddress() {
        // Test the DTO
        EntrepriseDTO entrepriseDTO = EntrepriseDTO.builder()
                .nom("Test Company DTO")
                .description("Test Description DTO")
                .address("456 DTO Street, DTO City, DTO Country")
                .type(EntrepriseType.EU)
                .build();
        
        assertNotNull(entrepriseDTO);
        assertEquals("Test Company DTO", entrepriseDTO.getNom());
        assertEquals("456 DTO Street, DTO City, DTO Country", entrepriseDTO.getAddress());
        assertEquals(EntrepriseType.EU, entrepriseDTO.getType());
    }

    @Test
    public void testEntrepriseMapperWithAddress() {
        // Test the mapper functionality
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setNom("Mapper Test Company");
        entreprise.setAddress("789 Mapper Street, Mapper City");
        entreprise.setType(EntrepriseType.EE);
        
        EntrepriseDTO dto = new EntrepriseDTO();
        
        // Create a simple mapper instance for testing with all required services
        EntrepriseMapper mapper = new EntrepriseMapper(null, null, null, null);
        mapper.setDTOFields(dto, entreprise);
        
        assertEquals(entreprise.getId(), dto.getId());
        assertEquals(entreprise.getNom(), dto.getNom());
        assertEquals(entreprise.getAddress(), dto.getAddress());
        assertEquals(entreprise.getType(), dto.getType());
    }
}
