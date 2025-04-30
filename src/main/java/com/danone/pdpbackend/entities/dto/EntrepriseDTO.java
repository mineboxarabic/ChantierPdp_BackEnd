package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.EntrepriseType;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseDTO {
    private Long id;
    private EntrepriseType type = EntrepriseType.EE; // ✅ Defines if it's EU or EE
    private String nom; // ✅ Name of the company
    private String description;
    private String numTel;
    private String raisonSociale;
    private ImageModel image; // ✅ For storing binary data (e.g., logos)
    private MedecinDuTravailleEE medecinDuTravailleEE;
    private List<Long> pdps; // ✅ If this entreprise is an EE, it has PDPs
    private List<Long> bdts; // ✅ If this entreprise is an EE, it has BDTs
    private List<Long> workers; // ✅ Workers employed by this entreprise
}
