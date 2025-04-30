package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ObjectAnswerRepo;
import com.danone.pdpbackend.Repo.PdpRepo;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.ObjectAnsweredMapper; // Assuming you have this mapper
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import com.danone.pdpbackend.entities.dto.PdpDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use Mockito extension for JUnit 5
class PdpServiceImplTest {

    // Mocks for all dependencies of PdpServiceImpl
    @Mock private PdpRepo pdpRepo;
    @Mock private EntrepriseService entrepriseService; // Assuming interface
    @Mock private ObjectAnswerRepo objectAnswerRepo;
    @Mock private RisqueService risqueService; // Assuming interface
    @Mock private DispositifService dispositifService; // Assuming interface
    @Mock private ChantierService chantierService; // Assuming interface
    @Mock private PdpMapper pdpMapper; // Mock the mapper
    @Mock private ObjectAnsweredMapper objectAnsweredMapper; // Mock if used within PdpService methods

    // Inject mocks into the service instance
    @InjectMocks private PdpServiceImpl pdpService;

    private Pdp pdp1;
    private Pdp pdp2;
    private PdpDTO pdpDTO1;
    private ObjectAnswered relation1;
    private ObjectAnswered relation2_updated;
    private ObjectAnswered relation3_new;
    private ObjectAnsweredDTO relationDTO1;


    @BeforeEach
    void setUp() {
        // Initialize common test objects
        pdp1 = new Pdp();
        pdp1.setId(1L);
        pdp1.setChantier(100L);
        pdp1.setRelations(new ArrayList<>()); // Initialize collections
        pdp1.setSignatures(new ArrayList<>());

        pdp2 = new Pdp();
        pdp2.setId(2L);
        pdp2.setChantier(101L);
        pdp2.setRelations(new ArrayList<>());
        pdp2.setSignatures(new ArrayList<>());

        pdpDTO1 = new PdpDTO();
        pdpDTO1.setId(1L);
        pdpDTO1.setChantier(100L);
        pdpDTO1.setRelations(new ArrayList<>());
        pdpDTO1.setSignatures(new ArrayList<>());

        relation1 = ObjectAnswered.builder().id(10L).pdp(pdp1).objectId(1L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build();
        relation2_updated = ObjectAnswered.builder().id(11L).pdp(pdp1).objectId(2L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build(); // Will be "updated"
        relation3_new = ObjectAnswered.builder().id(null).pdp(pdp1).objectId(3L).objectType(ObjectAnsweredObjects.RISQUE).answer(false).build(); // New

        relationDTO1 = ObjectAnsweredDTO.builder().id(10L).pdp(1L).objectId(1L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build();

    }

    @Test
    void getAll_shouldReturnListOfPdps() {
        // Arrange
        when(pdpRepo.findAll()).thenReturn(Arrays.asList(pdp1, pdp2));

        // Act
        List<Pdp> result = pdpService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pdpRepo, times(1)).findAll();
    }

    @Test
    void getById_whenPdpExists_shouldReturnPdp() {
        // Arrange
        when(pdpRepo.findById(1L)).thenReturn(Optional.of(pdp1));

        // Act
        Pdp result = pdpService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(pdpRepo, times(1)).findById(1L);
    }

    @Test
    void getById_whenPdpNotExists_shouldThrowException() {
        // Arrange
        when(pdpRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pdpService.getById(99L);
        });
        assertEquals("Pdp with id 99 not found", exception.getMessage());
        verify(pdpRepo, times(1)).findById(99L);
    }

    @Test
    void saveOrUpdatePdp_whenCreatingNew_shouldMapAndSave() {
        // Arrange
        PdpDTO newDto = new PdpDTO(); // Assume ID is null for creation
        newDto.setChantier(102L);
        newDto.setRelations(new ArrayList<>()); // Start empty
        newDto.setSignatures(new ArrayList<>());

        Pdp pdpFromMapper = new Pdp();
        pdpFromMapper.setChantier(102L);
        pdpFromMapper.setRelations(new ArrayList<>());
        pdpFromMapper.setSignatures(new ArrayList<>());

        Pdp savedPdp = new Pdp();
        savedPdp.setId(3L); // ID assigned after save
        savedPdp.setChantier(102L);
        savedPdp.setRelations(new ArrayList<>());
        savedPdp.setSignatures(new ArrayList<>());

        when(pdpMapper.toEntity(any(PdpDTO.class))).thenReturn(pdpFromMapper);
        when(pdpRepo.save(any(Pdp.class))).thenReturn(savedPdp);

        // Act
        Pdp result = pdpService.saveOrUpdatePdp(newDto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(pdpMapper, times(1)).toEntity(newDto);
        verify(pdpRepo, times(1)).save(pdpFromMapper);
        // Verify updateEntityFromDTO was NOT called
        verify(pdpMapper, never()).updateEntityFromDTO(any(Pdp.class), any(PdpDTO.class));
    }

    @Test
    void saveOrUpdatePdp_whenUpdating_shouldFetchMapAndUpdate() {
        // Arrange
        // pdpDTO1 has ID=1L
        Pdp existingPdp = pdp1; // Use pdp1 from setup which has ID=1L

        when(pdpRepo.findById(1L)).thenReturn(Optional.of(existingPdp));
        // Simulate mapper updating the existing entity in place
        // We don't strictly need to mock the internals of updateEntityFromDTO,
        // just that it's called and the result is saved.
        when(pdpRepo.save(any(Pdp.class))).thenReturn(existingPdp); // Assume save returns the updated entity

        // Act
        Pdp result = pdpService.saveOrUpdatePdp(pdpDTO1);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        // Verify the update path was taken
        verify(pdpRepo, times(1)).findById(1L);
        verify(pdpMapper, times(1)).updateEntityFromDTO(eq(existingPdp), eq(pdpDTO1));
        verify(pdpRepo, times(1)).save(existingPdp);
        // Verify toEntity was NOT called
        verify(pdpMapper, never()).toEntity(any(PdpDTO.class));
    }


    @Test
    void update_shouldMergeRelations() {
        // Arrange
        Pdp existingPdp = new Pdp();
        existingPdp.setId(1L);
        existingPdp.setRelations(new ArrayList<>(List.of(relation1))); // Starts with relation1

        ObjectAnswered incomingRelation1_updated = ObjectAnswered.builder().id(10L).pdp(null).objectId(1L).objectType(ObjectAnsweredObjects.RISQUE).answer(false).build(); // Update answer
        ObjectAnswered incomingRelation2_toDelete = ObjectAnswered.builder().id(11L).pdp(null).objectId(2L).objectType(ObjectAnsweredObjects.RISQUE).answer(null).build(); // Delete this one (has existing ID but null answer)
        ObjectAnswered incomingRelation3_new = ObjectAnswered.builder().id(null).pdp(null).objectId(3L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build(); // Add this one

        Pdp updatedPdpInput = new Pdp(); // This is the input to the service's update method
        updatedPdpInput.setId(1L);
        updatedPdpInput.setRelations(Arrays.asList(incomingRelation1_updated, incomingRelation2_toDelete, incomingRelation3_new));

        // Mocking repository calls within the merge logic
        when(pdpRepo.findById(1L)).thenReturn(Optional.of(existingPdp));
        when(objectAnswerRepo.findById(10L)).thenReturn(relation1); // Found existing relation1
        when(objectAnswerRepo.findById(11L)).thenReturn(relation2_updated); // Found existing relation2
        // For the new relation3, save will be called
        when(objectAnswerRepo.save(any(ObjectAnswered.class))).thenAnswer(invocation -> {
            ObjectAnswered arg = invocation.getArgument(0);
            if(arg.getId() == null) arg.setId(12L); // Simulate ID generation for new item
            return arg; // Return the saved item (potentially with ID)
        });

        when(pdpRepo.save(any(Pdp.class))).thenReturn(existingPdp); // Mock saving the final PDP


        // Act
        Pdp result = pdpService.update(1L, updatedPdpInput);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRelations().size(), "Should have 2 relations after merge (updated 1, added 1, deleted 1)");

        // Verify relation1 was updated (answer is false)
        ObjectAnswered finalRelation1 = result.getRelations().stream().filter(r -> r.getId() == 10L).findFirst().orElse(null);
        assertNotNull(finalRelation1);
        assertEquals(false, finalRelation1.getAnswer());

        // Verify relation3 was added (ID assigned by mock)
        ObjectAnswered finalRelation3 = result.getRelations().stream().filter(r -> r.getId() == 12L).findFirst().orElse(null);
        assertNotNull(finalRelation3);
        assertEquals(true, finalRelation3.getAnswer());
        assertEquals(3L, finalRelation3.getObjectId());


        // Verify interactions
        verify(pdpRepo, times(1)).findById(1L);
        verify(objectAnswerRepo, times(1)).findById(10L); // Checked existing r1
        verify(objectAnswerRepo, times(1)).findById(11L); // Checked existing r2
        verify(objectAnswerRepo, times(2)).save(any(ObjectAnswered.class)); // Saved updated r1 and new r3
        verify(objectAnswerRepo, times(1)).delete(any(ObjectAnswered.class)); // Deleted r2
        verify(pdpRepo, times(1)).save(existingPdp); // Saved the parent PDP
    }


    @Test
    void delete_whenPdpExists_shouldDeleteAndReturnTrue() {
        // Arrange
        when(pdpRepo.findById(1L)).thenReturn(Optional.of(pdp1));
        doNothing().when(pdpRepo).deleteById(1L); // Mock void method

        // Act
        Boolean result = pdpService.delete(1L);

        // Assert
        assertTrue(result);
        verify(pdpRepo, times(1)).findById(1L);
        verify(pdpRepo, times(1)).deleteById(1L);
    }

    @Test
    void delete_whenPdpNotExists_shouldReturnFalse() {
        // Arrange
        when(pdpRepo.findById(99L)).thenReturn(Optional.empty());

        // Act
        Boolean result = pdpService.delete(99L);

        // Assert
        assertFalse(result);
        verify(pdpRepo, times(1)).findById(99L);
        verify(pdpRepo, never()).deleteById(anyLong()); // Verify delete was not called
    }

    @Test
    void findWorkersByPdp_shouldReturnWorkers() {
        // Arrange
        Worker worker1 = new Worker(); worker1.setId(50L);
        Worker worker2 = new Worker(); worker2.setId(51L);
        pdp1.getSignatures().addAll(Arrays.asList(worker1, worker2));
        when(pdpRepo.findById(1L)).thenReturn(Optional.of(pdp1));

        // Act
        List<Worker> workers = pdpService.findWorkersByPdp(1L);

        // Assert
        assertNotNull(workers);
        assertEquals(2, workers.size());
        assertEquals(50L, workers.get(0).getId());
        verify(pdpRepo, times(1)).findById(1L);
    }

    @Test
    void getObjectAnsweredByPdpId_shouldReturnRelations() {
        // Arrange
        pdp1.getRelations().add(relation1); // Add one relation for testing
        when(pdpRepo.findById(1L)).thenReturn(Optional.of(pdp1));

        // Act
        // Test fetching for a specific type if logic differentiates, otherwise just fetch all
        List<ObjectAnswered> relations = pdpService.getObjectAnsweredsByPdpId(1L, ObjectAnsweredObjects.RISQUE); // Example type

        // Assert
        assertNotNull(relations);
        assertEquals(1, relations.size());
        assertEquals(10L, relations.get(0).getId());
        verify(pdpRepo, times(1)).findById(1L);
    }


    // Add more tests for other methods like getRecent, getLastId, getByIds etc. following the same pattern.

}