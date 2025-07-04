package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ObjectAnswerRepo;
import com.danone.pdpbackend.Repo.PdpRepo;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.ObjectAnsweredMapper; // Assuming you have this mapper
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
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
       // pdp1.setChantier(100L);
        pdp1.setRelations(new ArrayList<>()); // Initialize collections

        pdp2 = new Pdp();
        pdp2.setId(2L);
       // pdp2.setChantier(101L);
        pdp2.setRelations(new ArrayList<>());

        pdpDTO1 = new PdpDTO();
        pdpDTO1.setId(1L);
        pdpDTO1.setChantier(100L);
        pdpDTO1.setRelations(new ArrayList<>());
        pdpDTO1.setSignatures(new ArrayList<>());

        relation1 = ObjectAnswered.builder().id(10L).document(pdp1).objectId(1L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build();
        relation2_updated = ObjectAnswered.builder().id(11L).document(pdp1).objectId(2L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build(); // Will be "updated"
        relation3_new = ObjectAnswered.builder().id(null).document(pdp1).objectId(3L).objectType(ObjectAnsweredObjects.RISQUE).answer(false).build(); // New

        relationDTO1 = ObjectAnsweredDTO.builder().id(10L).document(1L).objectId(1L).objectType(ObjectAnsweredObjects.RISQUE).answer(true).build();

    }

}