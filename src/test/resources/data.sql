INSERT INTO users (email, fonction, notel, password, username) VALUES ('mineboxarabic@gmail.com', null, null, '$2a$10$S5AEzczg.gAFYNR9Gor9dOc35qwx9pX4fnvkjWV0J4ovu3w2m0P4y', 'Yassin4');

-- Sample AuditSecu data with typeOfAudit
INSERT INTO audit_secu (title, description, type_of_audit) VALUES 
('Équipe de sécurité', 'Responsable de la sécurité du chantier', 'intervenant'),
('Opérateur de machine', 'Opérateur qualifié pour les machines lourdes', 'intervenant'),
('Superviseur', 'Supervise les activités de construction', 'intervenant'),
('Marteau pneumatique', 'Outil certifié pour la démolition', 'outil'),
('Casque de sécurité', 'Équipement de protection individuelle', 'outil'),
('Harnais de sécurité', 'Protection contre les chutes', 'outil');
