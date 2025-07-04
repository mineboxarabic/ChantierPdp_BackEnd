package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.Utils.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "pdp")
@Getter
@Setter
public class Pdp extends Document {


    private Date dateInspection = new Date();
    private Date icpdate = new Date();
    private Date datePrevenirCSSCT = new Date(); // ✅ Notification date for CSSCT (if required)
    private Date datePrev = new Date();// ✅ Planned date for something (depends on business rules)

    private String horairesDetails;

    @ManyToOne
    private Entreprise entrepriseDInspection;

    @Embedded
    private HoraireDeTravaille horaireDeTravail;

    @Embedded
    private MisesEnDisposition misesEnDisposition;

    public Pdp(long l) {
        super.setId(l);
    }

    public Pdp() {

    }
}