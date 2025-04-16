package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.PdpRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PdpServiceImplTest {

    @Autowired
    private PdpRepo pdpRepo;

    @Autowired
    private PdpServiceImpl pdpService;

/*    @Test
    void testUpdatePdp_SuccessfulUpdate() {
        // Arrange: Create and save an initial Pdp entity
        Pdp existingPdp = new Pdp();
        existingPdp.setId(pdpRepo.findMaxId() + 1L);
        existingPdp.setOperation("Old Operation");
        existingPdp = pdpRepo.save(existingPdp);


        PdpUpdateDTO updatedPdp = new PdpUpdateDTO();
       //updatedPdp.setId(pdpRepo.findMaxId() + 1L);
        updatedPdp.setOperation("New Operation");

        // Act: Update the Pdp
        Pdp existingUpdated = pdpService.updatePdp(updatedPdp, existingPdp.getId());

        // Assert: Verify the updates
        assertNotNull(existingUpdated);
        assertEquals("New Operation", existingUpdated.getOperation());

        // Verify the entity is updated in the database
        Pdp updatedEntity = pdpRepo.findById(existingPdp.getId()).orElseThrow();
        assertEquals("New Operation", updatedEntity.getOperation());
    }

    @Test
    void testUpdatePdp_PartialUpdateSuccessful() {
        // Arrange: Create and save an initial Pdp entity
        Pdp existingPdp = new Pdp();
        existingPdp.setId(pdpRepo.findMaxId() + 1L);
        existingPdp.setOperation("Old Operation");
        existingPdp.setLieuintervention("Old Lieu");
        existingPdp.setDatedebuttravaux(new Date());
        existingPdp.setDatefintravaux(new Date());
        existingPdp.setEffectifmaxisurchantier(10);
        existingPdp.setNombreinterimaires(5);
        existingPdp.setHorairedetravail("Old Horaire");
        existingPdp.setHorairesdetail("Old Horaires");
        existingPdp.setIcpdate(new Date());
        existingPdp.setMedecintravaileu("Old Medecin EU");
        existingPdp.setMedecintravailee("Old Medecin EE");
        existingPdp.setDateprevenircssct(new Date());
        existingPdp.setDateprev(new Date());
        existingPdp.setLocation("Old Location");
        existingPdp = pdpRepo.save(existingPdp);


        PdpUpdateDTO updatedPdp = new PdpUpdateDTO();
        updatedPdp.setOperation("New Operation");

        // Act: Update the Pdp
        Pdp existingUpdated = pdpService.updatePdp(updatedPdp, existingPdp.getId());

        // Assert: Verify the updates
        assertNotNull(existingUpdated);
        assertEquals("New Operation", existingUpdated.getOperation());
        assertEquals("Old Lieu", existingUpdated.getLieuintervention());

        // Verify the entity is updated in the database
        Pdp updatedEntity = pdpRepo.findById(existingPdp.getId()).orElseThrow();

        assertEquals("New Operation", updatedEntity.getOperation());
        assertEquals("Old Lieu", updatedEntity.getLieuintervention());
    }

    @Test
    void testUpdatePdp_PdpNotFound() {
        // Arrange: Create an updated Pdp object
        PdpUpdateDTO updatedPdp = new PdpUpdateDTO();
        updatedPdp.setOperation("New Operation");

        Long nonExistentId = 999L;

        // Act & Assert: Try to update a non-existent Pdp and expect an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pdpService.updatePdp(updatedPdp, nonExistentId);
        });

        assertEquals("Pdp with id 999 not found", exception.getMessage());
    }


    @Test
    void testRecent_GetLast10Pdps() {
        //Arrange
        List<Pdp> pdps = pdpService.getRecent();
        //Act
        if(pdps.size() > 10){
            fail("List of pdps is greater than 10");
        }

        //Assert
        assertNotNull(pdps);
        assertEquals(10, pdps.size());
    }*/
}