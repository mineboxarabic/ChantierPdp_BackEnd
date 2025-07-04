package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers; // Import Mappers
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat; // Using AssertJ for fluent assertions

class AnalyseDeRisqueMapperTest {

    private final AnalyseDeRisqueMapper mapper = new AnalyseDeRisqueMapper();

    private AnalyseDeRisque entity;
    private AnalyseDeRisqueDTO dto;

    // private Risque risque; // Temporarily commented out

    @BeforeEach
    void setUp() {
        Risque risque = new Risque(); // Temporarily commented out
        risque.setId(10L); // Temporarily commented out
         risque.setTitle("Test Risque"); // Temporarily commented out

        entity = new AnalyseDeRisque();
        entity.setId(1L);
        entity.setDeroulementDesTaches("Initial task flow");
        entity.setMoyensUtilises("Initial tools");
        // entity.setRisque(risque); // Temporarily commented out
        entity.setMesuresDePrevention("Initial prevention measures");

        dto = new AnalyseDeRisqueDTO();
        dto.setId(2L);
        dto.setDeroulementDesTaches("DTO task flow");
        dto.setMoyensUtilises("DTO tools");
        dto.setRisque(risque); // Temporarily commented out
        dto.setMesuresDePrevention("DTO prevention measures");
    }

    @Test
    void shouldMapEntityToDtoCorrectly() {
        System.out.println("--- shouldMapEntityToDtoCorrectly (Simplified) ---");
        if (entity != null) {
            System.out.println("Entity before mapping: ID=" + entity.getId() + ", Tasks=" + entity.getDeroulementDesTaches());
        } else {
            System.out.println("Entity is null before mapping!");
        }

        AnalyseDeRisqueDTO resultDto = mapper.toDTO(entity);

        if (resultDto != null) {
            System.out.println("DTO after mapping: ID=" + resultDto.getId() + ", Tasks=" + resultDto.getDeroulementDesTaches());
        } else {
            System.out.println("Result DTO is null after mapping!");
        }
        System.out.println("-------------------------------------");

        assertThat(resultDto).isNotNull();
        // Check simple fields first
        assertThat(resultDto.getId()).isEqualTo(entity.getId());
        assertThat(resultDto.getDeroulementDesTaches()).isEqualTo(entity.getDeroulementDesTaches());
        assertThat(resultDto.getMoyensUtilises()).isEqualTo(entity.getMoyensUtilises());
        assertThat(resultDto.getMesuresDePrevention()).isEqualTo(entity.getMesuresDePrevention());
        // assertThat(resultDto.getRisque()).isEqualTo(entity.getRisque()); // Temporarily commented out
        // assertThat(resultDto.getRisque().getId()).isEqualTo(entity.getRisque().getId()); // Temporarily commented out
    }

    @Test
    void shouldUpdateEntityFromDtoCorrectly() {
        // Given
        // Create a fresh entity instance to update
        AnalyseDeRisque entityToUpdate = new AnalyseDeRisque();
        entityToUpdate.setId(1L); // Ensure it has the same ID as the original for update logic
        entityToUpdate.setDeroulementDesTaches("Old task flow");
        entityToUpdate.setMoyensUtilises("Old tools");
        Risque oldRisque = new Risque(); oldRisque.setId(99L);
        entityToUpdate.setRisque(oldRisque);
        entityToUpdate.setMesuresDePrevention("Old prevention");

        // When
        mapper.updateDTOFromEntity(dto, entityToUpdate); // Use the dto created in setUp

        // Then
        // Assert that fields in entityToUpdate now match the dto
        assertThat(entityToUpdate.getId()).isEqualTo(dto.getId()); // MapStruct updates ID by default
        assertThat(entityToUpdate.getDeroulementDesTaches()).isEqualTo(dto.getDeroulementDesTaches());
        assertThat(entityToUpdate.getMoyensUtilises()).isEqualTo(dto.getMoyensUtilises());
        assertThat(entityToUpdate.getRisque()).isEqualTo(dto.getRisque()); // Reference updated
        assertThat(entityToUpdate.getRisque().getId()).isEqualTo(dto.getRisque().getId()); // Nested property updated
        assertThat(entityToUpdate.getMesuresDePrevention()).isEqualTo(dto.getMesuresDePrevention());
    }

    @Test
    void shouldReturnNullDtoWhenEntityIsNull() {
        AnalyseDeRisqueDTO resultDto = mapper.toDTO(null);
        assertThat(resultDto).isNull();
    }

    @Test
    void shouldReturnNullEntityWhenDtoIsNull() {
        AnalyseDeRisque resultEntity = mapper.toEntity(null);
        assertThat(resultEntity).isNull();
    }

    @Test
    void updateEntityFromDtoShouldNotFailWhenDtoIsNull() {
        // Given
        AnalyseDeRisque entityToUpdate = new AnalyseDeRisque();
        entityToUpdate.setId(1L);
        String originalValue = "Original";
        entityToUpdate.setDeroulementDesTaches(originalValue);

        // When
        mapper.updateEntityFromDTO(entityToUpdate,null );

        // Then
        // Assert that the entity remains unchanged
        assertThat(entityToUpdate.getId()).isEqualTo(1L);
        assertThat(entityToUpdate.getDeroulementDesTaches()).isEqualTo(originalValue);

    }

    @Test
    void updateEntityFromDtoShouldNotFailWhenTargetEntityIsNull() {
        // This scenario typically shouldn't happen in practice with @MappingTarget,
        // but good to ensure MapStruct handles it gracefully (it should do nothing).
        // If MapStruct behavior changes, this test might need adjustment.
        try {
            mapper.updateEntityFromDTO( null,dto);
            // Expect no exception, or handle specific expected behavior if MapStruct throws one.
        } catch (Exception e) {
            // Fail if any unexpected exception occurs
            org.junit.jupiter.api.Assertions.fail("Unexpected exception during update with null target: " + e.getMessage());
        }
    }
}