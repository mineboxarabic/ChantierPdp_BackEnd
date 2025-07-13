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
    @JoinColumn(name = "worker_id", nullable = true)
    private Worker worker; // Link to the worker who signed


    @ManyToOne(fetch = FetchType.LAZY) // Use Lazy fetching
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // Link to the user who performed the signing action


    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date signatureDate;

    @Embedded // Embed the visual signature data
    private ImageModel signatureVisual;


    @Column(nullable = false)
    private boolean active = true; // Flag to handle "unsigning"

    @PrePersist
    protected void onCreate() {
        signatureDate = new Date(); // Set current date on creation
    }
}