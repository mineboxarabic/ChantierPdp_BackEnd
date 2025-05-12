package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.Utils.DocumentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PdpDTO extends DocumentDTO {
    private Date dateInspection;
    private Date icpdate;
    private Date datePrevenirCSSCT; // ✅ Notification date for CSSCT (if required)
    private Date datePrev; // ✅ Planned date for something (depends on business rules)

    private String horairesDetails;
    private Long entrepriseDInspection;

    private HoraireDeTravaille horaireDeTravail;
    private MisesEnDisposition misesEnDisposition;
}