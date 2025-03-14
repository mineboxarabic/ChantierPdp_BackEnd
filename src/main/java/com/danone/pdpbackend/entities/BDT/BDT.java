package com.danone.pdpbackend.entities.BDT;


import com.danone.pdpbackend.entities.AuditSecu;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Risque;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity(name = "BDT")
@Getter
@Setter
public class BDT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nom;


    @ManyToMany
    private List<ObjectAnswered> risques;

    @ManyToMany
    private List<ObjectAnswered> auditSecu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ComplementOuRappel> complementOuRappels;
}
