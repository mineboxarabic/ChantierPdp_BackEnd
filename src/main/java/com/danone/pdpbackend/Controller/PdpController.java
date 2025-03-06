package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.dto.PdpUpdateDTO;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pdp")
public class PdpController {


    PdpService pdpService;


    public PdpController(PdpService pdpService) {
        this.pdpService = pdpService;
    }


    @GetMapping("/all")
    public  ResponseEntity<ApiResponse<List<Pdp>>> getAllPdp()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getAllPdp(),"Pdps fetched successfully"));
    }

    //Create
    @PostMapping("/")
    public ResponseEntity<ApiResponse<Pdp>> savePdp(@RequestBody PdpUpdateDTO pdp) {
        Pdp createdPdp = pdpService.createPdp(pdp);
        return ResponseEntity.ok(new ApiResponse<>(createdPdp, "Pdp saved successfully"));
    }


    //Read
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pdp>> getPdp(@PathVariable Long id) {
        Pdp pdp = pdpService.getPdp(id);
        if (pdp == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Pdp not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(pdp, "Pdp fetched successfully"));
    }


    //Update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Pdp>> savePdp(@PathVariable Long id, @RequestBody Pdp pdp)
    {
        log.info("Updating pdp with id: {}", pdp.getOperation());
        Pdp pdpUpdated = pdpService.updatePdp(pdp, id);
        return ResponseEntity.ok(new ApiResponse<>(pdpUpdated, "Pdp updated successfully"));
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

}

