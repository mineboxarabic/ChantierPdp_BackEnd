package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.DocumentSignatureService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signature")
@RequiredArgsConstructor
public class SignatureController {

    private final DocumentSignatureService documentSignatureService;

/*    @PostMapping
    public ResponseEntity<ApiResponse<String>> signDocument(@RequestBody SignatureRequestDTO signatureRequest) {
        documentSignatureService.signDocument(signatureRequest);
        return ResponseEntity.ok(new ApiResponse<>(null,"Signature saved successfully"));
    }*/
}
