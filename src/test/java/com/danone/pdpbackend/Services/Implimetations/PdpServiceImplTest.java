package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdpServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(PdpServiceImplTest.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PdpRepo pdpRepo;

    @Mock
    private ChantierService chantierService;

    @Mock
    private EntrepriseService entrepriseService;

    @Mock
    private ObjectAnswerRepo objectAnswerRepo;

    @Mock
    private RisqueService risqueService;

    @Mock
    private DispositifService dispositifService;

    @Mock
    private ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo;

    @Mock
    private AnalyseDeRisqueRepo analyseDeRisqueRepo;

    @Mock
    private PermitRepo permitRepo;

    @Mock
    private ChantierRepo chantierRepo;

    @InjectMocks
    private PdpServiceImpl pdpService;

    private Pdp testPdp;

    @BeforeEach
    void setUp() {
        // Create test data
        testPdp = new Pdp();
        testPdp.setChantier(100L);
        testPdp.setDateInspection(new Date());
        testPdp.setIcpdate(new Date());
        testPdp.setDatePrevenirCSSCT(new Date());
        testPdp.setDatePrev(new Date());
        testPdp.setHorairesDetails("9:00 - 17:00");

/*        // Initialize empty lists
        testPdp.setRisques(new ArrayList<>());
        testPdp.setDispositifs(new ArrayList<>());
        testPdp.setPermits(new ArrayList<>());
        testPdp.setAnalyseDeRisques(new ArrayList<>());*/
        testPdp.setRelations(new ArrayList<>());
        testPdp.setSignatures(new ArrayList<>());
    }
/*
    @Test
    void createPdp_withValidData_returnsCreatedPdp() throws JsonProcessingException {
        // Given
        Pdp savedPdp = new Pdp();
        savedPdp.setId(1L); // Simulating DB generated ID
        savedPdp.setChantier(testPdp.getChantier());
        savedPdp.setRisques(new ArrayList<>());
        savedPdp.setDispositifs(new ArrayList<>());
        savedPdp.setPermits(new ArrayList<>());

        when(pdpRepo.save(any(Pdp.class))).thenReturn(savedPdp);

        // When
        Pdp result = pdpService.create(testPdp);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getChantier());
        String json = objectMapper.writeValueAsString(result);
        log.info("Here is the created Pdp {}", json);

        // Verify that save was called twice and chantier service was called
        verify(pdpRepo, times(2)).save(any(Pdp.class));
        verify(chantierService, times(1)).addPdpToChantier(eq(100L), any(Pdp.class));
    }

    @Test
    void createPdp_withValidDataAndWithRisques_returnsCreatedPdp() throws JsonProcessingException {
        // Given
        // Create test ObjectAnswered for risques
        ObjectAnswered risque1 = new ObjectAnswered();
        risque1.setRisque_id(1L);
        risque1.setAnswer(true);

        ObjectAnswered risque2 = new ObjectAnswered();
        risque2.setRisque_id(2L);
        risque2.setAnswer(false);

        // Add risques to test PDP
        testPdp.getRisques().add(risque1);
        testPdp.getRisques().add(risque2);

        // Create the expected saved PDP
        Pdp savedPdp = new Pdp();
        savedPdp.setId(1L);
        savedPdp.setChantier(testPdp.getChantier());
        savedPdp.setRisques(new ArrayList<>(testPdp.getRisques()));
        savedPdp.setDispositifs(new ArrayList<>());
        savedPdp.setPermits(new ArrayList<>());

        when(pdpRepo.save(any(Pdp.class))).thenReturn(savedPdp);

        // When
        Pdp result = pdpService.create(testPdp);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getChantier());
        assertNotNull(result.getRisques());

        assertEquals(2, result.getRisques().size());
        assertEquals(1L, result.getRisques().get(0).getRisque_id());
        assertEquals(2L, result.getRisques().get(1).getRisque_id());

        String json = objectMapper.writeValueAsString(result.getRisques());
        log.info("Here is the created Pdp with risques {}", json);

        verify(pdpRepo, times(2)).save(any(Pdp.class));
        verify(chantierService, times(1)).addPdpToChantier(eq(100L), any(Pdp.class));
    }

    @Test
    void createPdp_withAllTypesOfObjectAnswered_returnsCreatedPdp() throws JsonProcessingException {
        // Given
        // Create test ObjectAnswered for risques
        ObjectAnswered risque = new ObjectAnswered();
        risque.setRisque_id(1L);
        risque.setAnswer(true);
        testPdp.getRisques().add(risque);

        // Create test ObjectAnswered for dispositifs
        ObjectAnswered dispositif = new ObjectAnswered();
        dispositif.setDispositif_id(1L);
        dispositif.setAnswer(false);
        testPdp.getDispositifs().add(dispositif);

        // Create test ObjectAnswered for permits
        ObjectAnswered permit = new ObjectAnswered();
        permit.setPermit_id(1L);
        permit.setAnswer(true);
        testPdp.getPermits().add(permit);

        // Create test ObjectAnswered for analyse de risques
        ObjectAnswered analyseDeRisque = new ObjectAnswered();
        analyseDeRisque.setAnalyseDeRisque_id(1L);
        analyseDeRisque.setEE(true);
        analyseDeRisque.setEU(false);
        testPdp.getAnalyseDeRisques().add(analyseDeRisque);

        // Create the expected saved PDP
        Pdp savedPdp = new Pdp();
        savedPdp.setId(1L);
        savedPdp.setChantier(testPdp.getChantier());
        savedPdp.setRisques(new ArrayList<>(testPdp.getRisques()));
        savedPdp.setDispositifs(new ArrayList<>(testPdp.getDispositifs()));
        savedPdp.setPermits(new ArrayList<>(testPdp.getPermits()));
        savedPdp.setAnalyseDeRisques(new ArrayList<>(testPdp.getAnalyseDeRisques()));

        when(pdpRepo.save(any(Pdp.class))).thenReturn(savedPdp);

        // When
        Pdp result = pdpService.create(testPdp);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getChantier());

        // Verify risques
        assertNotNull(result.getRisques());
        assertEquals(1, result.getRisques().size());
        assertEquals(1L, result.getRisques().get(0).getRisque_id());

        // Verify dispositifs
        assertNotNull(result.getDispositifs());
        assertEquals(1, result.getDispositifs().size());
        assertEquals(1L, result.getDispositifs().get(0).getDispositif_id());

        // Verify permits
        assertNotNull(result.getPermits());
        assertEquals(1, result.getPermits().size());
        assertEquals(1L, result.getPermits().get(0).getPermit_id());

        // Verify analyse de risques
        assertNotNull(result.getAnalyseDeRisques());
        assertEquals(1, result.getAnalyseDeRisques().size());
        assertEquals(1L, result.getAnalyseDeRisques().get(0).getAnalyseDeRisque_id());
        assertEquals(true, result.getAnalyseDeRisques().get(0).getEE());
        assertEquals(false, result.getAnalyseDeRisques().get(0).getEU());

        String json = objectMapper.writeValueAsString(result);
        log.info("Here is the created Pdp with all types of ObjectAnswered {}", json);

        verify(pdpRepo, times(2)).save(any(Pdp.class));
        verify(chantierService, times(1)).addPdpToChantier(eq(100L), any(Pdp.class));
    }

    @Test
    void createPdp_withNullChantier_doesNotCallChantierService() {
        // Given
        testPdp.setChantier(null);

        Pdp savedPdp = new Pdp();
        savedPdp.setId(1L);
        savedPdp.setRisques(new ArrayList<>());
        savedPdp.setDispositifs(new ArrayList<>());
        savedPdp.setPermits(new ArrayList<>());

        when(pdpRepo.save(any(Pdp.class))).thenReturn(savedPdp);

        // When
        Pdp result = pdpService.create(testPdp);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getChantier());

        verify(pdpRepo, times(2)).save(any(Pdp.class));
        verify(chantierService, never()).addPdpToChantier(anyLong(), any(Pdp.class));
    }*/
}