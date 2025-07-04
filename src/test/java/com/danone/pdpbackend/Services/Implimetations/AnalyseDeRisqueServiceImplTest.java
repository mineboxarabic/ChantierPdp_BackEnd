package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.AnalyseDeRisqueRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerEntreprisesRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Utils.mappers.AnalyseDeRisqueMapper;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use Mockito extension for JUnit 5
class AnalyseDeRisqueServiceImplTest {

    @Mock // Create a mock instance of AnalyseDeRisqueRepo
    private AnalyseDeRisqueRepo analyseDeRisqueRepo;

    @Mock // Create a mock instance of RisqueRepo
    private RisqueRepo risqueRepo;

    @Mock // Create a mock instance of ObjectAnswerEntreprisesRepo
    private ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo;

    // Assuming you have a Mapper, if not, you might need to mock its behavior or use a real instance if simple
    // @Mock // Mock the mapper if its logic is complex or involves other dependencies
    // private AnalyseDeRisqueMapper analyseDeRisqueMapper;
    // Using real mapper instance for simplicity here, adjust if needed
    private final AnalyseDeRisqueMapper analyseDeRisqueMapper = new AnalyseDeRisqueMapper();


    @InjectMocks // Inject the mocks into AnalyseDeRisqueServiceImpl
    private AnalyseDeRisqueServiceImpl analyseDeRisqueService;

    private AnalyseDeRisque analyseDeRisque;
    private AnalyseDeRisqueDTO analyseDeRisqueDTO;
    private Risque risque;

    @BeforeEach
    void setUp() {
        // Initialize common test objects
        risque = new Risque();
        risque.setId(1L);
        risque.setTitle("Test Risque");

        analyseDeRisque = new AnalyseDeRisque();
        analyseDeRisque.setId(1L);
        analyseDeRisque.setDeroulementDesTaches("Test Tasks");
        analyseDeRisque.setMoyensUtilises("Test Tools");
        analyseDeRisque.setRisque(risque);
        analyseDeRisque.setMesuresDePrevention("Test Measures");

        analyseDeRisqueDTO = analyseDeRisqueMapper.toDTO(analyseDeRisque); // Use mapper
    }

    @Test
    void getAll() {
        // Given: Configure the mock repository to return a list of entities
        when(analyseDeRisqueRepo.findAll()).thenReturn(List.of(analyseDeRisque));

        // When: Call the service method
        List<AnalyseDeRisque> result = analyseDeRisqueService.getAll();

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(analyseDeRisque.getId(), result.get(0).getId());
        // Verify that the repository method was called exactly once
        verify(analyseDeRisqueRepo, times(1)).findAll();
    }

    @Test
    void getById_ExistingId_ShouldReturnEntity() {
        // Given: Configure mock repository to return the entity
        when(analyseDeRisqueRepo.findAnalyseDeRisqueById(anyLong())).thenReturn(analyseDeRisque);

        // When: Call the service method
        AnalyseDeRisque result = analyseDeRisqueService.getById(1L);

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(analyseDeRisque.getId(), result.getId());
        // Verify the repository method call
        verify(analyseDeRisqueRepo, times(1)).findAnalyseDeRisqueById(1L);
    }

    @Test
    void getById_NonExistingId_ShouldReturnNull() {
        // Given: Configure mock repository to return null (or Optional.empty() if repo returns Optional)
        when(analyseDeRisqueRepo.findAnalyseDeRisqueById(anyLong())).thenReturn(null);

        // When: Call the service method
        AnalyseDeRisque result = analyseDeRisqueService.getById(99L);

        // Then: Assert the result is null
        assertNull(result);
        // Verify the repository method call
        verify(analyseDeRisqueRepo, times(1)).findAnalyseDeRisqueById(99L);
    }

    @Test
    void create() {
        // Given: Configure the mock repository's save method
        // ArgumentCaptor can be used to capture the saved entity if needed for complex assertions
        when(analyseDeRisqueRepo.save(any(AnalyseDeRisque.class))).thenReturn(analyseDeRisque);

        // When: Call the service method to create
        // Create a new entity instance for creation, don't reuse the one from setUp which has an ID
        AnalyseDeRisque newAnalyse = new AnalyseDeRisque();
        newAnalyse.setDeroulementDesTaches("New Tasks");
        newAnalyse.setRisque(risque);

        AnalyseDeRisque result = analyseDeRisqueService.create(newAnalyse);

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(analyseDeRisque.getId(), result.getId()); // Should have the ID assigned by the mock save
        assertEquals(analyseDeRisque.getDeroulementDesTaches(), result.getDeroulementDesTaches());
        // Verify the repository save method was called once with the correct object type
        verify(analyseDeRisqueRepo, times(1)).save(any(AnalyseDeRisque.class));
    }

    @Test
    void update() {
        // Given: Prepare updated details and configure mock save
        AnalyseDeRisque updatedDetails = new AnalyseDeRisque();
        updatedDetails.setId(1L); // Must match the ID being updated
        updatedDetails.setDeroulementDesTaches("Updated Tasks");
        updatedDetails.setRisque(risque);
        updatedDetails.setMesuresDePrevention("Updated Measures");

        // Configure the mock repository to return the updated entity when save is called
        when(analyseDeRisqueRepo.save(any(AnalyseDeRisque.class))).thenReturn(updatedDetails);

        // When: Call the service update method
        AnalyseDeRisque result = analyseDeRisqueService.update(1L, updatedDetails);

        // Then: Assert the result matches the updated details
        assertNotNull(result);
        assertEquals(updatedDetails.getId(), result.getId());
        assertEquals("Updated Tasks", result.getDeroulementDesTaches());
        assertEquals("Updated Measures", result.getMesuresDePrevention());
        // Verify the repository save method was called once
        verify(analyseDeRisqueRepo, times(1)).save(any(AnalyseDeRisque.class));
    }


    @Test
    @DisplayName("deleteAnalyseDeRisque - When AnalyseDeRisque Exists - Should Delete Successfully")
    void deleteAnalyseDeRisque_WhenAnalyseDeRisqueExists_ShouldDeleteSuccessfully() {
        Long existingId = 1L; // ID of the entity to delete
        // Given: Mock existsById to return true
        when(analyseDeRisqueRepo.existsById(existingId)).thenReturn(true);
        // Mock deleteById to do nothing (it's void)
        doNothing().when(analyseDeRisqueRepo).deleteById(existingId);

        // When: Call the service delete method and assert no exception is thrown
        assertDoesNotThrow(() -> {
            analyseDeRisqueService.delete(existingId); // Corrected method name
        }, "Deletion should not throw an exception when entity exists, nom d'un chien! (Good heavens!)");

        // Then: Verify interactions
        verify(analyseDeRisqueRepo, times(1)).existsById(existingId);
        verify(analyseDeRisqueRepo, times(1)).deleteById(existingId);
    }

    @Test
    @DisplayName("deleteAnalyseDeRisque - When AnalyseDeRisque Does Not Exist - Should Throw EntityNotFoundException")
    void deleteAnalyseDeRisque_WhenAnalyseDeRisqueDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Given: Mock existsById to return false
        Long nonExistingId = 99L; // ID of the entity that does not exist
        when(analyseDeRisqueRepo.existsById(nonExistingId)).thenReturn(false);

        // When & Then: Call the service delete method and assert EntityNotFoundException is thrown
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            analyseDeRisqueService.delete(nonExistingId); // Corrected method name
        }, "Should throw EntityNotFoundException if entity does not exist. Sacrebleu!");

        assertTrue(exception.getMessage().contains("Analyse De RisqueRepo with id " + nonExistingId + " not found"));

        // Verify that existsById was called
        verify(analyseDeRisqueRepo, times(1)).existsById(nonExistingId);
        // Verify that deleteById was NOT called
        verify(analyseDeRisqueRepo, never()).deleteById(anyLong());
    }


}