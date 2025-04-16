package com.danone.pdpbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "object_answered")
@Getter
@Setter
public class ObjectAnswered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    Long risque_id;

    Long dispositif_id;

    Long permit_id;

    Long auditSecu_id;

    Boolean answer;


}
