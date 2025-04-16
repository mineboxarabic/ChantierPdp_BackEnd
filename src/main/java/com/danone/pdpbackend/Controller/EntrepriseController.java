package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.EntrepriseMapper;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/entreprise")
public class EntrepriseController {


    EntrepriseService entrepriseService;


    @Autowired
    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    //CRUD


    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Entreprise>>> fetchAll(){
        return ResponseEntity.ok(new ApiResponse<>(entrepriseService.findAll(),"Entreprises fetched successfully"));
    }



    //Create
    @PostMapping("")
    public ResponseEntity<String> create(@RequestBody Entreprise entreprise)
    {
        entrepriseService.create(entreprise);
        return ResponseEntity.ok("Entreprise saved successfully");

    }


    //Read
    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseDTO> getEntreprise(@PathVariable Long id)
    {
        Entreprise entreprise = entrepriseService.getEntrepriseById(id);

        if( entreprise == null){
            return ResponseEntity.badRequest().body(null).notFound().build();
        }
        return ResponseEntity.ok(EntrepriseMapper.toDTO(entreprise));
    }

    //Update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Entreprise>> updateEntreprise(@PathVariable Long id, @RequestBody Entreprise entrepriseDto)
    {
        Entreprise entreprise = entrepriseService.getEntrepriseById(id);
        if(entreprise == null){
            return ResponseEntity.badRequest().body(null).notFound().build();
        }
        entrepriseService.updateEntreprise(entrepriseDto, id);
        return ResponseEntity.ok(new ApiResponse<>(entreprise, "Entreprise updated successfully"));
    }

    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteEntreprise(@PathVariable Long id)
    {
        if(!entrepriseService.deleteEntreprise(id)){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Entreprise not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "Entreprise deleted successfully"));
    }


    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<Entreprise>>> createMultiple(@RequestBody List<Long> entreprise_ids)
    {
        List<Entreprise> entreprises = entrepriseService.getEntreprisesByIds(entreprise_ids);
        return ResponseEntity.ok(new ApiResponse<>(entreprises, "Entreprises saved successfully"));
    }


    //    const getWorkersByEntreprise = async (entrepriseId: number): Promise<Worker[]> => {

    @GetMapping("/{entrepriseId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersByEntreprise(@PathVariable Long entrepriseId)
    {
        return ResponseEntity.ok(new ApiResponse<>(entrepriseService.getWorkersByEntreprise(entrepriseId), "Workers fetched successfully"));
    }

}
