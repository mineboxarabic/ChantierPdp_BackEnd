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
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getAllPdp().stream().map(pdpMapper::toDto).toList(),"Pdps fetched successfully"));
    }

    //Create
    @PostMapping("/")
    public ResponseEntity<ApiResponse<PdpDTO>> savePdp(@RequestBody PdpDTO pdpDTO) {
        Pdp createdPdp = pdpService.createPdp(pdpMapper.toEntity(pdpDTO));
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDto(createdPdp), "Pdp saved successfully"));
    }


    //Read
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PdpDTO>> getPdp(@PathVariable Long id) {
        Pdp pdp = pdpService.getPdp(id);
        if (pdp == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Pdp not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDto(pdp), "Pdp fetched successfully"));
    }


    //Update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PdpDTO>> savePdp(@PathVariable Long id, @RequestBody PdpDTO pdpDTO)
    {
        Pdp pdpUpdated = pdpService.updatePdp(pdpMapper.toEntity(pdpDTO), id);
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDto(pdpUpdated), "Pdp updated successfully"));
    }



    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePdp(@PathVariable Long id)
    {
        if(!pdpService.deletePdp(id)){
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

    //api/pdp/' + pdpId + '/risque/' + risqueId, 'POST',
    @PostMapping("/{pdpId}/risque/{risqueId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addRisqueToPdp(@PathVariable Long pdpId, @PathVariable Long risqueId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addObjectAnswered(pdpId, risqueId, ObjectAnsweredObjects.RISQUE), "Risque added to pdp successfully"));
    }

    @PostMapping("/{pdpId}/dispositif/{dispositifId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addDispositifToPdp(@PathVariable Long pdpId, @PathVariable Long dispositifId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addObjectAnswered(pdpId, dispositifId, ObjectAnsweredObjects.DISPOSITIF), "Dispositif added to pdp successfully"));
    }

    //etch('api/pdp/' + pdpId + '/analyse/' + analyseId, 'POST', null,
    @PostMapping("/{pdpId}/analyse/{analyseId}")
    public ResponseEntity<ApiResponse<ObjectAnsweredEntreprises>> addAnalyseToPdp(@PathVariable Long pdpId, @PathVariable Long analyseId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addAnalyseToPdp(pdpId, analyseId), "Analyse added to pdp successfully"));
    }

//        return fetch('api/pdp/' + pdpId + '/permit/' + permitId, 'POST', null,

    @PostMapping("/{pdpId}/permit/{permitId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addPermitToPdp(@PathVariable Long pdpId, @PathVariable Long permitId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addObjectAnswered(pdpId, permitId, ObjectAnsweredObjects.PERMIT), "Permit added to pdp successfully"));
    }


    //return fetch('api/pdp/' + pdpId + '/permit/' + permitId, 'DELETE', null,
    @DeleteMapping("/{pdpId}/permit/{permitId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> removePermitFromPdp(@PathVariable Long pdpId, @PathVariable Long permitId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.removeObjectAnswered(pdpId, permitId, ObjectAnsweredObjects.PERMIT), "Permit removed from pdp successfully"));
    }



    @PostMapping("/{pdpId}/object/{objectId}/type/{objectType}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addObjectToPdp(@PathVariable Long pdpId, @PathVariable Long objectId, @PathVariable ObjectAnsweredObjects objectType)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.addObjectAnswered(pdpId, objectId,objectType), "Object added to pdp successfully"));
    }


    //return fetch('api/pdp/' + pdpId + '/permit/' + permitId, 'DELETE', null,
    @DeleteMapping("/{pdpId}/object/{objectId}/type/{objectType}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> removeObjectFromPdp(@PathVariable Long pdpId, @PathVariable Long objectId, @PathVariable ObjectAnsweredObjects objectType)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.removeObjectAnswered(pdpId, objectId,objectType), "Object removed from pdp successfully"));
    }


    @PostMapping("/sign/{pdpId}")
    public ResponseEntity<ApiResponse<String>> signPdp(@PathVariable Long pdpId)
    {
        return ResponseEntity.ok(new ApiResponse<>(null, "Pdp signed successfully"));
    }


    @GetMapping("/exist/{id}")
    public ResponseEntity<ApiResponse<Boolean>> existPdp(@PathVariable Long id)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getPdp(id) != null, "Pdp exist"));
    }


    //        return fetch('api/pdp/' + pdpId + '/analyse/' + analyseId, 'DELETE', null,
    @DeleteMapping("/{pdpId}/analyse/{analyseId}")
    public ResponseEntity<ApiResponse<ObjectAnsweredEntreprises>> removeAnalyseFromPdp(@PathVariable Long pdpId, @PathVariable Long analyseId)
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.removeAnalyse(pdpId, analyseId), "Analyse removed from pdp successfully"));
    }

    //        return fetch(`api/pdp/${pdpId}/workers`, 'GET', null, [
    @GetMapping("/pdp/{pdpId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersByPdpId(@PathVariable Long pdpId) {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.findWorkersByPdp(pdpId), "Workers fetched"));
    }



    //Make a get to get teh risques of a pdp

    @GetMapping("/{pdpId}/objectAnswered/{objectType}")
    public ResponseEntity<ApiResponse<List<ObjectAnswered>>> getObjectAnsweredByPdpId(@PathVariable Long pdpId, @PathVariable ObjectAnsweredObjects objectType) {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getObjectAnsweredByPdpId(pdpId, objectType), "Risques fetched"));
    }

}

