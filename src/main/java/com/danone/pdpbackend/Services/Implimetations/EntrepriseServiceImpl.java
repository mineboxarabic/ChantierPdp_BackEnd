package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.EntrepriseRepo;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Utils.EntrepriseMapper;
import com.danone.pdpbackend.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.Entreprise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EntrepriseServiceImpl implements EntrepriseService {

    EntrepriseRepo entrepriseRepo;

    public EntrepriseServiceImpl(EntrepriseRepo entrepriseRepo) {
        this.entrepriseRepo = entrepriseRepo;
    }

    @Override
    public void create(Entreprise entreprise) {



        log.info("Last id: {}", entrepriseRepo.findMaxId());
        entreprise.setId(entrepriseRepo.findMaxId() + 1L);
        entrepriseRepo.save(entreprise);
    }



    @Override
    public Entreprise findEntrepriseByNom(String danone) {
        return entrepriseRepo.findEntrepriseByNom(danone);
    }

    @Override
    public Long findMaxId() {
        return entrepriseRepo.findMaxId();
    }

    @Override
    public Entreprise findEntrepriseById(Long id) {
        return entrepriseRepo.findEntrepriseById(id);
    }

    @Override
    public void createEntreprise(Entreprise entreprise) {
        entreprise.setId(entrepriseRepo.findMaxId() + 1L);
        entrepriseRepo.save(entreprise);
    }

    @Override
    public Entreprise createEntreprise(EntrepriseDTO entrepriseDto) {
        Entreprise entreprise = EntrepriseMapper.toEntity(entrepriseDto);
        entreprise.setId(entrepriseRepo.findMaxId() + 1L);
        return entrepriseRepo.save(entreprise);
    }

    @Override
    public Entreprise updateEntreprise(Entreprise entrepriseutilisatrice, Long id) {
        //Pach update
        Entreprise entreprise = entrepriseRepo.findEntrepriseById(id);
        if (entreprise == null) {
            throw new IllegalArgumentException("Entreprise with id " + id + " not found");
        }

        if (entrepriseutilisatrice.getNom() != null) entreprise.setNom(entrepriseutilisatrice.getNom());
       // if (entrepriseutilisatrice.getFonction() != null) entreprise.setFonction(entrepriseutilisatrice.getFonction());
        if (entrepriseutilisatrice.getNumTel() != null) entreprise.setNumTel(entrepriseutilisatrice.getNumTel());
       // if (entrepriseutilisatrice.getReferentPdp() != null) entreprise.setReferentPdp(entrepriseutilisatrice.getReferentPdp());
       // if (entrepriseutilisatrice.getResponsableChantier() != null) entreprise.setResponsableChantier(entrepriseutilisatrice.getResponsableChantier());
        if (entrepriseutilisatrice.getRaisonSociale() != null) entreprise.setRaisonSociale(entrepriseutilisatrice.getRaisonSociale());
        if(entrepriseutilisatrice.getImage() != null) {
            entreprise.setImage(entrepriseutilisatrice.getImage());
        }
       // if(entrepriseutilisatrice.getIsUtilisatrice() != null) entreprise.setIsUtilisatrice(entrepriseutilisatrice.getIsUtilisatrice());

        entrepriseRepo.save(entreprise);
        return entreprise;

    }

    @Override
    public Entreprise updateEntreprise(EntrepriseDTO entrepriseDTO, Long id) {

        Entreprise entreprise = entrepriseRepo.findEntrepriseById(id);
        if (entreprise == null) {
            throw new IllegalArgumentException("Entreprise with id " + id + " not found");
        }

        if (entrepriseDTO.getNom() != null)         entreprise.setNom(entrepriseDTO.getNom());
      //  if (entrepriseDTO.getFonction() != null)    entreprise.setFonction(entrepriseDTO.getFonction());
        if (entrepriseDTO.getNumTel() != null)      entreprise.setNumTel(entrepriseDTO.getNumTel());
        //if (entrepriseDTO.getReferentPdp() != null) entreprise.setReferentPdp(entrepriseDTO.getReferentPdp());
      //  if (entrepriseDTO.getResponsableChantier() != null) entreprise.setResponsableChantier(entrepriseDTO.getResponsableChantier());
        if (entrepriseDTO.getRaisonSociale() != null) entreprise.setRaisonSociale(entrepriseDTO.getRaisonSociale());
        entrepriseRepo.save(entreprise);
        return entreprise;
    }

    @Override
    public List<Entreprise> findAll() {
        return entrepriseRepo.findAll();
    }

    @Override
    public boolean deleteEntreprise(Long id) {
        Entreprise entreprise = entrepriseRepo.findEntrepriseById(id);
        if (entreprise == null) {
            return false;
        }
        entrepriseRepo.delete(entreprise);
        return true;
    }
}
