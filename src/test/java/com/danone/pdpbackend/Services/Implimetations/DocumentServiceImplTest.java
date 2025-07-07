package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Services.ChantierService;
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

    private Document document;

    private Chantier chantier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chantier = new Chantier();
        chantier.setId(1L);
        chantier.setNom("Test Chantier");


        when(chantierService.getById(1L)).thenReturn(chantier); // Mock chantierService behavior

        Worker worker = new Worker();
        worker.setId(1L);
        worker.setPrenom("John");
        worker.setNom("Doe");
        when(workerSelectionService.getWorkersForChantier(1L)).thenReturn(List.of(worker)); // Mock workerSelectionService behavior

        document = new Pdp(); // Use a concrete subclass of Document
        document.setId(1L);
        document.setChantier(chantier); // Add chantier to the document
        document.setRelations(List.of(
                new ObjectAnswered(1L, ObjectAnsweredObjects.RISQUE, true, 1L, null), // Set valid objectId
                new ObjectAnswered(2L, ObjectAnsweredObjects.RISQUE, true, 2L, null)  // Set valid objectId
        ));

        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(worker);
        document.setSignatures(List.of(signature)); // Add signatures to the document
    }

    @Test
    void testCalculateDocumentState_PermitMissing() {
        // Mock Risque objects
        Risque risque1 = new Risque();
        risque1.setId(1L);
        risque1.setTravaillePermit(true);
        risque1.setPermitId(null); // Missing permit

        Risque risque2 = new Risque();
        risque2.setId(2L);
        risque2.setTravaillePermit(false);

        when(risqueService.getRisqueById(1L)).thenReturn(risque1);
        when(risqueService.getRisqueById(2L)).thenReturn(risque2);

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
    void testCalculateDocumentState_AllPermitsProvided() {
        // Mock Risque objects
        Risque risque1 = new Risque();
        risque1.setId(1L);
        risque1.setTravaillePermit(true);
        risque1.setPermitId(100L); // Permit provided

        Risque risque2 = new Risque();
        risque2.setId(2L);
        risque2.setTravaillePermit(false);

        when(risqueService.getRisqueById(1L)).thenReturn(risque1);
        when(risqueService.getRisqueById(2L)).thenReturn(risque2);

        // Call the method
        Document result = documentService.calculateDocumentState(document);

        // Verify the state and action type
        assertEquals(DocumentStatus.ACTIVE, result.getStatus());
        assertEquals(ActionType.NONE, result.getActionType());

        // Verify interactions
        verify(risqueService, times(1)).getRisqueById(1L);
        verify(risqueService, times(1)).getRisqueById(2L);
    }
}
