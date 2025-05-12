package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
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
    private Document document;

    @Enumerated(EnumType.STRING)
    private ObjectAnsweredObjects objectType; // "Srisque", "dispositif", "permit", ...

    private Long objectId;

    //Answers
    Boolean answer;

    Boolean ee;

    Boolean eu;


}
