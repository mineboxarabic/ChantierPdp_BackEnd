package com.danone.pdpbackend.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class Document {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
}
