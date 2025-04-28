package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.AnalyseDeRisqueRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerEntreprisesRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Utils.mappers.AnalyseDeRisqueMapper;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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
    void getAllAnalyseDeRisques() {
        // Given: Configure the mock repository to return a list of entities
        when(analyseDeRisqueRepo.findAll()).thenReturn(List.of(analyseDeRisque));

        // When: Call the service method
        List<AnalyseDeRisque> result = analyseDeRisqueService.getAllAnalyseDeRisques();

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(analyseDeRisque.getId(), result.get(0).getId());
        // Verify that the repository method was called exactly once
        verify(analyseDeRisqueRepo, times(1)).findAll();
    }

    @Test
    void getAnalyseDeRisqueById_ExistingId_ShouldReturnEntity() {
        // Given: Configure mock repository to return the entity
        when(analyseDeRisqueRepo.findAnalyseDeRisqueById(anyLong())).thenReturn(analyseDeRisque);

        // When: Call the service method
        AnalyseDeRisque result = analyseDeRisqueService.getAnalyseDeRisqueById(1L);

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(analyseDeRisque.getId(), result.getId());
        // Verify the repository method call
        verify(analyseDeRisqueRepo, times(1)).findAnalyseDeRisqueById(1L);
    }

    @Test
    void getAnalyseDeRisqueById_NonExistingId_ShouldReturnNull() {
        // Given: Configure mock repository to return null (or Optional.empty() if repo returns Optional)
        when(analyseDeRisqueRepo.findAnalyseDeRisqueById(anyLong())).thenReturn(null);

        // When: Call the service method
        AnalyseDeRisque result = analyseDeRisqueService.getAnalyseDeRisqueById(99L);

        // Then: Assert the result is null
        assertNull(result);
        // Verify the repository method call
        verify(analyseDeRisqueRepo, times(1)).findAnalyseDeRisqueById(99L);
    }

    @Test
    void createAnalyseDeRisque() {
        // Given: Configure the mock repository's save method
        // ArgumentCaptor can be used to capture the saved entity if needed for complex assertions
        when(analyseDeRisqueRepo.save(any(AnalyseDeRisque.class))).thenReturn(analyseDeRisque);

        // When: Call the service method to create
        // Create a new entity instance for creation, don't reuse the one from setUp which has an ID
        AnalyseDeRisque newAnalyse = new AnalyseDeRisque();
        newAnalyse.setDeroulementDesTaches("New Tasks");
        newAnalyse.setRisque(risque);

        AnalyseDeRisque result = analyseDeRisqueService.createAnalyseDeRisque(newAnalyse);

        // Then: Assert the result
        assertNotNull(result);
        assertEquals(analyseDeRisque.getId(), result.getId()); // Should have the ID assigned by the mock save
        assertEquals(analyseDeRisque.getDeroulementDesTaches(), result.getDeroulementDesTaches());
        // Verify the repository save method was called once with the correct object type
        verify(analyseDeRisqueRepo, times(1)).save(any(AnalyseDeRisque.class));
    }

    @Test
    void updateAnalyseDeRisque() {
        // Given: Prepare updated details and configure mock save
        AnalyseDeRisque updatedDetails = new AnalyseDeRisque();
        updatedDetails.setId(1L); // Must match the ID being updated
        updatedDetails.setDeroulementDesTaches("Updated Tasks");
        updatedDetails.setRisque(risque);
        updatedDetails.setMesuresDePrevention("Updated Measures");

        // Configure the mock repository to return the updated entity when save is called
        when(analyseDeRisqueRepo.save(any(AnalyseDeRisque.class))).thenReturn(updatedDetails);

        // When: Call the service update method
        AnalyseDeRisque result = analyseDeRisqueService.updateAnalyseDeRisque(1L, updatedDetails);

        // Then: Assert the result matches the updated details
        assertNotNull(result);
        assertEquals(updatedDetails.getId(), result.getId());
        assertEquals("Updated Tasks", result.getDeroulementDesTaches());
        assertEquals("Updated Measures", result.getMesuresDePrevention());
        // Verify the repository save method was called once
        verify(analyseDeRisqueRepo, times(1)).save(any(AnalyseDeRisque.class));
    }


    @Test
    void deleteAnalyseDeRisque_ExistingId_ShouldReturnTrue() {
        // Given: Mock existsById to return true and doNothing for the void deleteById
        when(analyseDeRisqueRepo.existsById(1L)).thenReturn(true); // Simulate finding the entity via existence check

        // CORRECTED MOCKING: Use doNothing for the void method
        doNothing().when(analyseDeRisqueRepo).deleteById(1L);

        // When: Call the service delete method
        Boolean result = analyseDeRisqueService.deleteAnalyseDeRisque(1L);

        // Then: Assert the result is true and verify interactions
        assertTrue(result);
        verify(analyseDeRisqueRepo, times(1)).existsById(1L); // Verify existence check
        verify(analyseDeRisqueRepo, times(1)).deleteById(1L); // Verify delete was called
    }

    @Test
    void deleteAnalyseDeRisque_NonExistingId_ShouldReturnFalse() {
        // Given: Mock existsById to return false
        when(analyseDeRisqueRepo.existsById(99L)).thenReturn(false); // Simulate not finding

        // When: Call the service delete method
        Boolean result = analyseDeRisqueService.deleteAnalyseDeRisque(99L);

        // Then: Assert the result is false and verify deleteById was NOT called
        assertFalse(result);
        verify(analyseDeRisqueRepo, times(1)).existsById(99L); // Verify existence check
        verify(analyseDeRisqueRepo, never()).deleteById(anyLong()); // Verify delete was NOT called
    }
}