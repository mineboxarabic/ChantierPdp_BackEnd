package com.danone.pdpbackend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "permit")
@Getter
@Setter
public class Permit extends InfoDeBase{
}
