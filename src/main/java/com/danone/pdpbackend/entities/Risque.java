package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.PermiTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;


@Entity(name = "risque")
@Getter
@Setter
public class Risque extends InfoDeBase{
    private Boolean travailleDangereux;
    private Boolean travaillePermit;
    public Long permitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permit_type")
    private PermiTypes permitType;

    private Date createdAt;
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
