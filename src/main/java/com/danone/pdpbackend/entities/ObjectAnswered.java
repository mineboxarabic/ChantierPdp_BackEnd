package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.Bdt;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "object_answered")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectAnswered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdp_id", nullable = false)
    private Pdp pdp;

    @ManyToOne
    @JoinColumn(name = "bdt_id", nullable = false)
    private Bdt bdt;

    @Enumerated(EnumType.STRING)
    private ObjectAnsweredObjects objectType; // "Srisque", "dispositif", "permit", ...

    private Long objectId;

    //Answers
    Boolean answer;

    Boolean ee;

    Boolean eu;


}
