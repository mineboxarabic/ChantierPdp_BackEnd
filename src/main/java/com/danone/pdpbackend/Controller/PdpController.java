package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pdp")
public class PdpController {

    PdpService pdpService;

    PdpMapper pdpMapper;

    public PdpController(PdpService pdpService, PdpMapper pdpMapper) {
        this.pdpService = pdpService;
        this.pdpMapper = pdpMapper;
    }


    @GetMapping("/all")
    public  ResponseEntity<ApiResponse<List<PdpDTO>>> getAllPdp()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getAll().stream().map(pdpMapper::toDTO).toList(),"Pdps fetched successfully"));
    }

    //Create
    @PostMapping("/")
    public ResponseEntity<ApiResponse<PdpDTO>> savePdp(@RequestBody PdpDTO pdpDTO) {
        Pdp createdPdp = pdpService.saveOrUpdatePdp(pdpDTO);
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDTO(createdPdp), "Pdp saved successfully"));
    }


    //Read
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PdpDTO>> getPdp(@PathVariable Long id) {
        Pdp pdp = pdpService.getById(id);
        if (pdp == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Pdp not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDTO(pdp), "Pdp fetched successfully"));
    }


    //Update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PdpDTO>> savePdp(@PathVariable Long id, @RequestBody PdpDTO pdpDTO)
    {
        Pdp pdpUpdated = pdpService.update(id, pdpMapper.toEntity(pdpDTO));
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDTO(pdpUpdated), "Pdp updated successfully"));
    }



    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePdp(@PathVariable Long id)
    {
        if(!pdpService.delete(id)){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Pdp not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "Pdp deleted successfully"));
    }

    //Get last id
    @GetMapping("/last")
    public ResponseEntity<ApiResponse<Long>> getLastId()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getLastId(), "Last id fetched successfully"));
    }


    //Get last 10 pdps
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Pdp>>> getRecent()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getRecent(), "Recent pdps fetched successfully"));
    }


    @PostMapping("/sign/{pdpId}")
    public ResponseEntity<ApiResponse<String>> signPdp(@PathVariable Long pdpId)
    {
        return ResponseEntity.ok(new ApiResponse<>(null, "Pdp signed successfully"));
    }


    @GetMapping("/exist/{id}")
    public ResponseEntity<ApiResponse<Boolean>> existPdp(@PathVariable Long id)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getById(id) != null, "Pdp exist"));
    }


    //        return fetch(`api/pdp/${pdpId}/workers`, 'GET', null, [
    @GetMapping("/pdp/{pdpId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersByPdpId(@PathVariable Long pdpId) {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.findWorkersByPdp(pdpId), "Workers fetched"));
    }


/*    //ObjectAnswered
    @PostMapping("/{pdpId}/object-answred/{objectId}/type/{objectType}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addObjectToPdp(@PathVariable Long pdpId, @RequestBody ObjectAnswered objectAnswered, @PathVariable ObjectAnsweredObjects objectType)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addObjectAnswered(pdpId, objectAnswered,objectType), "Object added to pdp successfully"));
    }

    @DeleteMapping("/{pdpId}/object-answered/{objectId}/type/{objectType}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> removeObjectFromPdp(@PathVariable Long pdpId, @PathVariable Long objectId, @PathVariable ObjectAnsweredObjects objectType)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.removeObjectAnswered(pdpId, objectId,objectType), "Object removed from pdp successfully"));
    }

    @PostMapping("/{pdpId}/object-answered/multiple/type/{objectType}")
    public ResponseEntity<ApiResponse<List<ObjectAnswered>>> addMultipleObjectsToPdp(@PathVariable Long pdpId, @RequestBody List<ObjectAnswered> objectAnswereds, @PathVariable ObjectAnsweredObjects objectType){
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addMultipleObjectsToPdp(pdpId, objectAnswereds, objectType), "Multiple objects are linked"));
    }*/


    //Make a get to get teh risques of a pdp
    @GetMapping("/{pdpId}/object-answered/{objectType}")
    public ResponseEntity<ApiResponse<List<ObjectAnswered>>> getObjectAnsweredByPdpId(@PathVariable Long pdpId, @PathVariable ObjectAnsweredObjects objectType) {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getObjectAnsweredByPdpId(pdpId, objectType), "items fetched"));
    }

  /*  @DeleteMapping("/{pdpId}/object-answered/multiple/type/{objectType}")
    public ResponseEntity<ApiResponse<List<ObjectAnswered>>> removeMultipleObjectsFromPdp(@PathVariable Long pdpId, @RequestBody List<Long> objectIds, @PathVariable ObjectAnsweredObjects objectType)
    {
        log.info("It's in the remove", objectIds);
        return ResponseEntity.ok(new ApiResponse<>(pdpService.removeMultipleObjectsFromPdp(pdpId, objectIds,objectType), "feij"));
    }*/

}

