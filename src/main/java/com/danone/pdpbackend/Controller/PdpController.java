package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.ObjectAnsweredMapper;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pdp")
public class PdpController {

    private final ObjectAnsweredMapper objectAnsweredMapper;
    PdpService pdpService;

    PdpMapper pdpMapper;

    public PdpController(PdpService pdpService, PdpMapper pdpMapper, ObjectAnsweredMapper objectAnsweredMapper) {
        this.pdpService = pdpService;
        this.pdpMapper = pdpMapper;
        this.objectAnsweredMapper = objectAnsweredMapper;
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
        Pdp existingPdp = pdpService.getById(id);
        Pdp pdpUpdated = pdpService.update(id, pdpMapper.updateEntityFromDTO(existingPdp,pdpDTO));
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDTO(pdpUpdated), "Pdp updated successfully"));
    }



    //Delete
    @DeleteMapping("/{id}")
    public void deletePdp(@PathVariable Long id)
    {
      pdpService.delete(id);
        //return ResponseEntity.ok(new ApiResponse<>(null, "Pdp deleted successfully"));
    }

    //Get last id
    @GetMapping("/last")
    public ResponseEntity<ApiResponse<Long>> getLastId()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpService.getLastId(), "Last id fetched successfully"));
    }


    //Get last 10 pdps
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PdpDTO>>> getRecent()
    {
        return ResponseEntity.ok(new ApiResponse<>(pdpMapper.toDTOList(pdpService.getRecent()), "Recent pdps fetched successfully"));
    }









    //Make a get to get teh risques of a pdp
    @GetMapping("/{pdpId}/object-answered/{objectType}")
    public ResponseEntity<ApiResponse<List<ObjectAnsweredDTO>>> getObjectAnsweredsByPdpId(@PathVariable Long pdpId, @PathVariable ObjectAnsweredObjects objectType) {
        return ResponseEntity.ok(new ApiResponse<>(objectAnsweredMapper.toDTOList(pdpService.getObjectAnsweredsByPdpId(pdpId, objectType)), "items fetched"));
    }




}
