package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Utils.ApiResponse;
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


    private final com.danone.pdpbackend.Utils.mappers.EntrepriseMapper entrepriseMapper;
    EntrepriseService entrepriseService;


    @Autowired
    public EntrepriseController(EntrepriseService entrepriseService, com.danone.pdpbackend.Utils.mappers.EntrepriseMapper entrepriseMapper) {
        this.entrepriseService = entrepriseService;
        this.entrepriseMapper = entrepriseMapper;
    }

    //CRUD


    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EntrepriseDTO>>> fetchAll(){
        return ResponseEntity.ok(new ApiResponse<>(entrepriseMapper.toDTOList(entrepriseService.getAll()),"Entreprises fetched successfully"));
    }



    //Create
    @PostMapping("")
    public ResponseEntity<ApiResponse<EntrepriseDTO>> create(@RequestBody EntrepriseDTO entrepriseDTO)
    {
        Entreprise createdEntreprise = entrepriseService.create(entrepriseMapper.toEntity(entrepriseDTO));
        return ResponseEntity.ok(new ApiResponse<>(entrepriseMapper.toDTO(createdEntreprise), "Entreprise created successfully"));
    }


    //Read
    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseDTO> getEntreprise(@PathVariable Long id)
    {
        Entreprise entreprise = entrepriseService.getById(id);

        if( entreprise == null){
            return ResponseEntity.badRequest().body(null).notFound().build();
        }
        return ResponseEntity.ok(entrepriseMapper.toDTO(entreprise));
    }

    //Update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EntrepriseDTO>> updateEntreprise(@PathVariable Long id, @RequestBody EntrepriseDTO entrepriseDto)
    {
        Entreprise entreprise = entrepriseService.getById(id);
        if(entreprise == null){
            return ResponseEntity.badRequest().body(null).notFound().build();
        }
        Entreprise updatedEntreprise = entrepriseService.update(id, entrepriseMapper.updateEntityFromDTO(entreprise, entrepriseDto));
        return ResponseEntity.ok(new ApiResponse<>(entrepriseMapper.toDTO(updatedEntreprise), "Entreprise updated successfully"));
    }

    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteEntreprise(@PathVariable Long id)
    {
        if(!entrepriseService.delete(id)){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Entreprise not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "Entreprise deleted successfully"));
    }


    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<Entreprise>>> createMultiple(@RequestBody List<Long> entreprise_ids)
    {
        List<Entreprise> entreprises = entrepriseService.getByIds(entreprise_ids);
        return ResponseEntity.ok(new ApiResponse<>(entreprises, "Entreprises saved successfully"));
    }


    //    const getWorkersByEntreprise = async (entrepriseId: number): Promise<Worker[]> => {

    @GetMapping("/{entrepriseId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersByEntreprise(@PathVariable Long entrepriseId)
    {
        return ResponseEntity.ok(new ApiResponse<>(entrepriseService.getWorkersByEntreprise(entrepriseId), "Workers fetched successfully"));
    }

}
