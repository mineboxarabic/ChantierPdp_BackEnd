package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.Utils.ChantierStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ChantierMapperTestTest {

    @Mock private EntrepriseService entrepriseService;
    @Mock private LocalisationService localisationService;
    @Mock private UserService userService;
    @Mock private BdtService bdtService;
    @Mock private PdpService pdpService;
    @Mock private WorkerService workerService;
    @Mock private WorkerSelectionService workerSelectionService;

    @InjectMocks
    private ChantierMapper mapper = new ChantierMapperImpl(); // MapStruct will generate this

    @Test
    public void testToEntity() {
        ChantierDTO dto = new ChantierDTO();
        dto.setId(1L);
        dto.setNom("Test Chantier");
        dto.setOperation("Opération A");
        dto.setDateDebut(new Date());
        dto.setDateFin(new Date());
        dto.setNbHeurs(8);
        dto.setIsAnnuelle(true);
        dto.setEffectifMaxiSurChantier(10);
        dto.setNombreInterimaires(5);
        dto.setStatus(ChantierStatus.ACTIVE);
        dto.setTravauxDangereux(true);

        dto.setEntrepriseUtilisatrice(100L);
        dto.setLocalisation(200L);
        dto.setDonneurDOrdre(300L);
        dto.setEntrepriseExterieurs(List.of(101L, 102L));
        dto.setBdts(List.of(201L));
        dto.setPdps(List.of(301L));
        dto.setWorkerSelections(List.of(501L));

        when(entrepriseService.getById(100L)).thenReturn(new Entreprise(100L));
        when(localisationService.getById(200L)).thenReturn(new Localisation(200L));
        when(userService.getUserById(300L)).thenReturn(new User(300L));
        when(entrepriseService.getByIds(List.of(101L, 102L))).thenReturn(List.of(new Entreprise(101L), new Entreprise(102L)));
        when(bdtService.getByIds(List.of(201L))).thenReturn(List.of(new Bdt(201L)));
        when(pdpService.getByIds(List.of(301L))).thenReturn(List.of(new Pdp(301L)));
        when(workerService.getWorkersByIds(List.of(401L))).thenReturn(List.of(new Worker(401L)));
        when(workerSelectionService.getWorkerSelectionsByIds(List.of(501L))).thenReturn(List.of(new WorkerChantierSelection(501L)));

        Chantier entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getNom()).isEqualTo("Test Chantier");
        assertThat(entity.getEntrepriseUtilisatrice().getId()).isEqualTo(100L);
        assertThat(entity.getEntrepriseExterieurs()).hasSize(2);
        assertThat(entity.getBdts().get(0).getId()).isEqualTo(201L);
    }

    @Test
    public void testToDTO() {
        Chantier chantier = new Chantier();
        chantier.setId(2L);
        chantier.setNom("Chantier DTO");
        chantier.setOperation("Op B");
        chantier.setDateDebut(new Date());
        chantier.setDateFin(new Date());
        chantier.setNbHeurs(10);
        chantier.setIsAnnuelle(false);
        chantier.setEffectifMaxiSurChantier(20);
        chantier.setNombreInterimaires(7);
        chantier.setStatus(ChantierStatus.ACTIVE);
        chantier.setTravauxDangereux(false);

        chantier.setEntrepriseUtilisatrice(new Entreprise(111L));
        chantier.setEntrepriseExterieurs(List.of(new Entreprise(112L), new Entreprise(113L)));
        chantier.setLocalisation(new Localisation(222L));
        chantier.setDonneurDOrdre(new User(333L));
        chantier.setBdts(List.of(new Bdt(444L)));
        chantier.setPdps(List.of(new Pdp(555L)));
        chantier.setWorkerSelections(List.of(new WorkerChantierSelection(777L)));

        ChantierDTO dto = mapper.toDTO(chantier);

        assertThat(dto).isNotNull();
        assertThat(dto.getNom()).isEqualTo("Chantier DTO");
        assertThat(dto.getEntrepriseUtilisatrice()).isEqualTo(111L);
        assertThat(dto.getBdts()).containsExactly(444L);
    }
}
