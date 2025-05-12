package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.Image.ImageModel; // Assuming ImageModel exists
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity(name = "document_signature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Use Lazy fetching
    @JoinColumn(name = "document_id", nullable = false)
    private Document document; // Link back to the specific document (Pdp, Bdt, etc.)

    @ManyToOne(fetch = FetchType.LAZY) // Use Lazy fetching
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker; // Link to the worker who signed

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date signatureDate;

    @Embedded // Embed the visual signature data
    private ImageModel signatureVisual;

    // Optional: Add a field to denote the role if needed for specific cases
    // E.g., for BDTs, you might still want to know if it was 'ChargeDeTravail'
    // private String signerRole;

    @Column(nullable = false)
    private boolean active = true; // Flag to handle "unsigning"

    @PrePersist
    protected void onCreate() {
        signatureDate = new Date(); // Set current date on creation
    }
}