package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.DocumentSignatureService;
import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.Services.WorkerSelectionService;
import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DocumentServiceImplTest {

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Mock
    private RisqueService risqueService;

    @Mock
    private ChantierService chantierService;

    @Mock
    private WorkerSelectionService workerSelectionService;

    @Mock
    private DocumentSignatureService documentSignatureService;

    private Document document;
    private Chantier chantier;
    private User donneurDOrdre;
    private Worker worker1;
    private Worker worker2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create donneur d'ordre
        donneurDOrdre = new User();
        donneurDOrdre.setId(100L);
        donneurDOrdre.setUsername("chef.manager");
        donneurDOrdre.setEmail("chef@danone.com");
        donneurDOrdre.setFonction("Chef de Chantier");

        // Create chantier with donneur d'ordre
        chantier = new Chantier();
        chantier.setId(1L);
        chantier.setNom("Test Chantier");
        chantier.setDonneurDOrdre(donneurDOrdre);

        // Create workers
        worker1 = new Worker();
        worker1.setId(1L);
        worker1.setPrenom("John");
        worker1.setNom("Doe");

        worker2 = new Worker();
        worker2.setId(2L);
        worker2.setPrenom("Jane");
        worker2.setNom("Smith");

        when(chantierService.getById(1L)).thenReturn(chantier);
        when(workerSelectionService.getWorkersForChantier(1L)).thenReturn(Arrays.asList(worker1, worker2));

        // Create document
        document = new Pdp();
        document.setId(1L);
        document.setChantier(chantier);
        document.setDonneurDOrdre(donneurDOrdre);
        document.setRelations(Arrays.asList(
                new ObjectAnswered(1L, ObjectAnsweredObjects.RISQUE, true, 1L, null),
                new ObjectAnswered(2L, ObjectAnsweredObjects.RISQUE, true, 2L, null)
        ));
        
        // Default: no signatures
        document.setSignatures(new ArrayList<>());
    }

    @Test
    void testCalculateDocumentState_PermitMissing() {
        // Mock Risque objects - one requires a permit, one doesn't
        Risque risque1 = new Risque();
        risque1.setId(1L);
        risque1.setTravaillePermit(true);
        risque1.setPermitType(com.danone.pdpbackend.Utils.PermiTypes.HAUTEUR); // Requires HAUTEUR permit

        Risque risque2 = new Risque();
        risque2.setId(2L);
        risque2.setTravaillePermit(false);

        when(risqueService.getRisqueById(1L)).thenReturn(risque1);
        when(risqueService.getRisqueById(2L)).thenReturn(risque2);
        
        // Mock signatures to be complete so permit check is reached
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre));

        // Call the method
        Document result = documentService.calculateDocumentState(document);

        // Verify the state and action type
        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.PERMIT_MISSING, result.getActionType());

        // Verify interactions
        verify(risqueService, times(1)).getRisqueById(1L);
        verify(risqueService, times(1)).getRisqueById(2L);
    }

    @Test
    void testCalculateDocumentState_NoSignatures_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // No signatures provided
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(new ArrayList<>());

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_OnlyWorkerSignatures_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // Only workers signed, donneur d'ordre not signed
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(new ArrayList<>());

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_OnlyDonneurDOrdreSignature_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // Only donneur d'ordre signed, workers not signed
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre));

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_PartialWorkerSignatures_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // Only one worker signed, donneur d'ordre signed
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1)); // Only worker1 signed
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre));

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_AllWorkersSignedButNoDonneurDOrdre_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // All workers signed but donneur d'ordre not signed  
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(new ArrayList<>()); // Donneur d'ordre not signed

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_AllSignatures_Active() {
        setupRisquesWithoutPermitRequirement();
        
        // All workers AND donneur d'ordre signed
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre));

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.ACTIVE, result.getStatus());
        assertEquals(ActionType.NONE, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_NoDonneurDOrdreInDocument_SignaturesMissing() {
        setupRisquesWithoutPermitRequirement();
        
        // Document without donneur d'ordre
        document.setDonneurDOrdre(null);
        
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(new ArrayList<>());

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_EmptyWorkerSelection_StillRequiresDonneurDOrdre() {
        setupRisquesWithoutPermitRequirement();
        
        // No workers assigned to chantier, but donneur d'ordre still needs to sign
        when(workerSelectionService.getWorkersForChantier(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(new ArrayList<>()); // Donneur d'ordre not signed

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.NEEDS_ACTION, result.getStatus());
        assertEquals(ActionType.SIGHNATURES_MISSING, result.getActionType());
    }

    @Test 
    void testCalculateDocumentState_EmptyWorkerSelection_WithDonneurDOrdreSignature_Active() {
        setupRisquesWithoutPermitRequirement();
        
        // No workers assigned to chantier, but donneur d'ordre has signed
        when(workerSelectionService.getWorkersForChantier(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(new ArrayList<>());
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre)); // Donneur d'ordre signed

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.ACTIVE, result.getStatus());
        assertEquals(ActionType.NONE, result.getActionType());
    }

    @Test
    void testCalculateDocumentState_BdtDocument_SameBehavior() {
        setupRisquesWithoutPermitRequirement();
        
        // Test with BDT document instead of PDP
        document = new Bdt();
        document.setId(1L);
        document.setChantier(chantier);
        document.setDonneurDOrdre(donneurDOrdre);
        document.setRelations(Arrays.asList(
                new ObjectAnswered(1L, ObjectAnsweredObjects.RISQUE, true, 1L, null),
                new ObjectAnswered(2L, ObjectAnsweredObjects.RISQUE, true, 2L, null)
        ));
        document.setSignatures(new ArrayList<>());
        
        // All signatures present
        when(documentSignatureService.getSignedWorkersByDocument(1L)).thenReturn(Arrays.asList(worker1, worker2));
        when(documentSignatureService.getSignedUsersByDocument(1L)).thenReturn(Arrays.asList(donneurDOrdre));

        Document result = documentService.calculateDocumentState(document);

        assertEquals(DocumentStatus.ACTIVE, result.getStatus());
        assertEquals(ActionType.NONE, result.getActionType());
    }

    private void setupRisquesWithoutPermitRequirement() {
        // Mock Risques that don't require permits
        Risque risque1 = new Risque();
        risque1.setId(1L);
        risque1.setTravaillePermit(false);

        Risque risque2 = new Risque();
        risque2.setId(2L);
        risque2.setTravaillePermit(false);

        when(risqueService.getRisqueById(1L)).thenReturn(risque1);
        when(risqueService.getRisqueById(2L)).thenReturn(risque2);
    }
}


